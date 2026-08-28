/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;

/**
 * Tests the data-only CSV profile decoder.
 */
public class TestProfileXmlDecoder
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Existing XMLEncoder profiles and shared serializers remain readable.
   */
  @Test
  public void decodeProfiles() throws Exception
  {
    DefaultSerializer serializer = new DefaultSerializer();
    Profile first = new Profile();
    first.setName("First");
    first.setFileEncoding("ISO-8859-1");
    first.setInvert(true);
    first.setQuotingChar("'");
    first.setSeparatorChar(",");
    first.setSkipLines(2);
    first.getColumns().add(new Column("name","Name",0,serializer));
    first.getColumns().add(new Column("iban","IBAN",1,serializer));
    Profile second = new Profile();
    second.setName("Second");
    second.getColumns().add(new Column("name","Name",0,serializer));

    List<Profile> decoded = decode(encode(first,second));
    Assert.assertEquals(2,decoded.size());
    Profile actual = decoded.get(0);
    Assert.assertEquals(first.getName(),actual.getName());
    Assert.assertEquals(first.getFileEncoding(),actual.getFileEncoding());
    Assert.assertEquals(first.isInvert(),actual.isInvert());
    Assert.assertEquals(first.getQuotingChar(),actual.getQuotingChar());
    Assert.assertEquals(first.getSeparatorChar(),actual.getSeparatorChar());
    Assert.assertEquals(first.getSkipLines(),actual.getSkipLines());
    Assert.assertEquals(2,actual.getColumns().size());
    Assert.assertEquals("iban",actual.getColumns().get(1).getProperty());
    Assert.assertSame(actual.getColumns().get(0).getSerializer(),actual.getColumns().get(1).getSerializer());
    Assert.assertEquals("Second",decoded.get(1).getName());
    Assert.assertSame(actual.getColumns().get(0).getSerializer(),decoded.get(1).getColumns().get(0).getSerializer());
  }

  /**
   * XMLDecoder instructions must be rejected without their side effects.
   */
  @Test
  public void rejectExecutableXml() throws Exception
  {
    Path proof = folder.getRoot().toPath().resolve("proof.txt");
    String xml = "<java version=\"21\" class=\"java.beans.XMLDecoder\">"
        + "<object class=\"de.willuhn.jameica.hbci.io.csv.Profile\">"
        + "<void property=\"columns\"><object class=\"java.io.PrintWriter\">"
        + "<string>" + proof + "</string><void method=\"close\"/>"
        + "</object></void></object></java>";

    try
    {
      decode(xml);
      Assert.fail("executable XML accepted");
    }
    catch (Exception expected)
    {
      Assert.assertFalse(Files.exists(proof));
    }
  }

  /**
   * External entities are not part of the profile format.
   */
  @Test
  public void rejectDoctype() throws Exception
  {
    Path secret = folder.newFile("secret.txt").toPath();
    Files.writeString(secret,"EXTERNAL_ENTITY_EXPANDED");
    String xml = "<!DOCTYPE java [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>"
        + "<java class=\"java.beans.XMLDecoder\">"
        + "<object class=\"de.willuhn.jameica.hbci.io.csv.Profile\">"
        + "<void property=\"name\"><string>&xxe;</string></void>"
        + "</object></java>";
    try
    {
      decode(xml);
      Assert.fail("DOCTYPE accepted");
    }
    catch (Exception expected)
    {
      // expected
    }
  }

  private static List<Profile> decode(String xml) throws Exception
  {
    return ProfileXmlDecoder.decode(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  private static String encode(Profile... profiles) throws Exception
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (XMLEncoder encoder = new XMLEncoder(bytes))
    {
      for (Profile profile:profiles)
        encoder.writeObject(profile);
    }
    return bytes.toString(StandardCharsets.UTF_8.name());
  }
}
