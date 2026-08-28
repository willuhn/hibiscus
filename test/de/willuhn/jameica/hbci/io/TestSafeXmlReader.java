/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpServer;

/**
 * Tests secure parsing and compatibility of the legacy XML reader.
 */
public class TestSafeXmlReader
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Representative values and multiple objects remain readable.
   */
  @Test
  public void readValidObjects() throws Exception
  {
    String xml = "<objects>"
        + "<object type=\"example.First\" id=\"1\">"
        + "<text type=\"java.lang.String\">hello</text>"
        + "<count type=\"java.lang.Integer\">7</count>"
        + "<amount type=\"java.math.BigDecimal\">12.34</amount>"
        + "<enabled type=\"java.lang.Boolean\">true</enabled>"
        + "<date type=\"java.util.Date\">01.02.2020 12:34:56</date>"
        + "<timestamp type=\"java.sql.Timestamp\">02.03.2021 01:02:03</timestamp>"
        + "</object>"
        + "<object type=\"example.Second\"><text>second</text></object>"
        + "</objects>";
    List<String> types = new ArrayList<String>();
    List<Map> values = new ArrayList<Map>();
    SafeXmlReader reader = reader(xml,(type,id,data) -> {
      types.add(type);
      values.add(data);
      return null;
    });
    reader.read();
    reader.read();
    Assert.assertNull(reader.read());
    reader.close();

    Assert.assertEquals("example.First",types.get(0));
    Assert.assertEquals("hello",values.get(0).get("text"));
    Assert.assertEquals(Integer.valueOf(7),values.get(0).get("count"));
    Assert.assertEquals(new BigDecimal("12.34"),values.get(0).get("amount"));
    Assert.assertEquals(Boolean.TRUE,values.get(0).get("enabled"));
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    Assert.assertEquals(format.parse("01.02.2020 12:34:56"),values.get(0).get("date"));
    Assert.assertEquals(new Timestamp(format.parse("02.03.2021 01:02:03").getTime()),values.get(0).get("timestamp"));
    Assert.assertEquals("second",values.get(1).get("text"));
  }

  /**
   * Direct external file entities must be rejected.
   */
  @Test
  public void rejectExternalFileEntity() throws Exception
  {
    Path secret = folder.newFile("secret.txt").toPath();
    Files.writeString(secret,"EXTERNAL_ENTITY_EXPANDED");
    assertRejected("<!DOCTYPE objects [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>"
        + xml("&xxe;"));
  }

  /**
   * External HTTP entities and DTDs must cause no network request.
   */
  @Test
  public void rejectExternalHttpResources() throws Exception
  {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/external.dtd",exchange -> {
      requests.incrementAndGet();
      byte[] response = "<!ENTITY xxe 'loaded'>".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200,response.length);
      exchange.getResponseBody().write(response);
      exchange.close();
    });
    server.start();
    try
    {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/external.dtd";
      assertRejected("<!DOCTYPE objects SYSTEM \"" + url + "\">" + xml("value"));
      assertRejected("<!DOCTYPE objects [<!ENTITY % remote SYSTEM \"" + url + "\">%remote;]>" + xml("value"));
      Assert.assertEquals(0,requests.get());
    }
    finally
    {
      server.stop(0);
    }
  }

  /**
   * XInclude processing stays disabled.
   */
  @Test
  public void doNotLoadXInclude() throws Exception
  {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/value",exchange -> {
      requests.incrementAndGet();
      exchange.sendResponseHeaders(200,0);
      exchange.close();
    });
    server.start();
    try
    {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/value";
      String xml = "<objects xmlns:xi=\"http://www.w3.org/2001/XInclude\">"
          + "<object type=\"example.Type\"><text><xi:include href=\"" + url + "\" parse=\"text\"/></text></object>"
          + "</objects>";
      SafeXmlReader reader = reader(xml,(type,id,values) -> null);
      reader.read();
      reader.close();
      Assert.assertEquals(0,requests.get());
    }
    finally
    {
      server.stop(0);
    }
  }

  /**
   * Entity expansion payloads fail at the DOCTYPE without material expansion.
   */
  @Test
  public void rejectEntityExpansion() throws Exception
  {
    long start = System.nanoTime();
    assertRejected("<!DOCTYPE objects ["
        + "<!ENTITY a '1234567890'>"
        + "<!ENTITY b '&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;'>"
        + "<!ENTITY c '&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;'>"
        + "]>" + xml("&c;"));
    Assert.assertTrue("entity payload took too long",System.nanoTime() - start < 2_000_000_000L);
  }

  private static SafeXmlReader reader(String xml, de.willuhn.datasource.serialize.ObjectFactory factory) throws Exception
  {
    return new SafeXmlReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),factory);
  }

  private static void assertRejected(String xml) throws Exception
  {
    try
    {
      reader(xml,(type,id,values) -> null);
      Assert.fail("unsafe XML accepted");
    }
    catch (Exception expected)
    {
      // expected
    }
  }

  private static String xml(String value)
  {
    return "<objects><object type=\"example.Type\" id=\"1\"><text type=\"java.lang.String\">"
        + value + "</text></object></objects>";
  }
}
