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
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import net.n3.nanoxml.IXMLElement;

/**
 * Tests the Moneyplex XML security boundary.
 */
public class TestMoneyplexParser
{
  /** Configured legacy encoding and ordinary Moneyplex structure are retained. */
  @Test
  public void parseValidLegacyDocument() throws Exception
  {
    String xml = "<KONTOBUCH><BUCHUNG><EMPFAENGER><NAME>M\u00fcller</NAME>"
        + "</EMPFAENGER></BUCHUNG></KONTOBUCH>";
    Charset encoding = Charset.forName("ISO-8859-1");
    IXMLElement root = MoneyplexParser.parse(new ByteArrayInputStream(xml.getBytes(encoding)),encoding.name());
    IXMLElement booking = root.getFirstChildNamed("BUCHUNG");
    Assert.assertEquals("M\u00fcller",booking.getFirstChildNamed("EMPFAENGER")
        .getFirstChildNamed("NAME").getContent());
  }

  /** External DTDs are rejected before a network request is attempted. */
  @Test
  public void rejectExternalDocumentTypeWithoutNetworkAccess() throws Exception
  {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/external.dtd",exchange -> {
      requests.incrementAndGet();
      exchange.sendResponseHeaders(200,0);
      exchange.close();
    });
    server.start();
    try
    {
      String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/external.dtd";
      String xml = "<!DOCTYPE KONTOBUCH SYSTEM \"" + url + "\"><KONTOBUCH/>";
      try
      {
        MoneyplexParser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),"UTF-8");
        Assert.fail("unsafe Moneyplex document accepted");
      }
      catch (Exception expected)
      {
        Assert.assertTrue(expected.getMessage().contains("DOCTYPE"));
      }
      Assert.assertEquals(0,requests.get());
    }
    finally
    {
      server.stop(0);
    }
  }
}
