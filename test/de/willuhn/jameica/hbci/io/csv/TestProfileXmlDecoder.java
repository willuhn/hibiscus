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
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the data-only CSV profile decoder.
 */
public class TestProfileXmlDecoder
{
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Existing XMLEncoder profiles and shared serializers remain readable.
   * @throws Exception
   */
  @Test
  public void decodeLecagyProfiles() throws Exception
  {
    final String xml = """
    <?xml version="1.0" encoding="UTF-8"?>
    <java version="17.0.17" class="java.beans.XMLDecoder">
     <object class="de.willuhn.jameica.hbci.io.csv.Profile" id="Profile0">
      <void property="columns">
       <void method="add">
        <object class="de.willuhn.jameica.hbci.io.csv.Column">
         <void property="column"><int>0</int></void>
         <void property="name"><string>Name</string></void>
         <void property="property"><string>name</string></void>
         <void property="serializer"><object class="de.willuhn.jameica.hbci.io.ser.DefaultSerializer" id="DefaultSerializer0"/></void>
        </object>
       </void>
       <void method="add">
        <object class="de.willuhn.jameica.hbci.io.csv.Column">
         <void property="column"><int>1</int></void>
         <void property="name"><string>IBAN</string></void>
         <void property="property"><string>iban</string></void>
         <void property="serializer"><object idref="DefaultSerializer0"/></void>
        </object>
       </void>
      </void>
      <void property="fileEncoding"><string>ISO-8859-1</string></void>
      <void property="invert"><boolean>true</boolean></void>
      <void property="name"><string>First</string></void>
      <void property="quotingChar"><string>&apos;</string></void>
      <void property="separatorChar"><string>,</string></void>
      <void property="skipLines"><int>2</int></void>
     </object>
     <object class="de.willuhn.jameica.hbci.io.csv.Profile" id="Profile1">
      <void property="columns">
       <void method="add">
        <object class="de.willuhn.jameica.hbci.io.csv.Column">
         <void property="column"><int>0</int></void>
         <void property="name"><string>Name</string></void>
         <void property="property"><string>name</string></void>
         <void property="serializer"><object idref="DefaultSerializer0"/></void>
        </object>
       </void>
      </void>
      <void property="name"><string>Second</string></void>
     </object>
    </java>
    """;
    
    List<Profile> decoded = decode(xml);
    Assert.assertEquals(2,decoded.size());
    Profile actual = decoded.get(0);
    Assert.assertEquals("First",actual.getName());
    Assert.assertEquals("ISO-8859-1",actual.getFileEncoding());
    Assert.assertEquals(true,actual.isInvert());
    Assert.assertEquals("'",actual.getQuotingChar());
    Assert.assertEquals(",",actual.getSeparatorChar());
    Assert.assertEquals(2,actual.getSkipLines());
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

  /**
   * Decodiert die Profile explizit im alten Format.
   * @param xml das XML.
   * @return die Profile.
   * @throws Exception
   */
  private static List<Profile> decode(String xml) throws Exception
  {
    return LegacyProfileXmlDecoder.decode(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Schreibt Profile explizit im alten Format.
   * @param profiles die Profile.
   * @return die serialisierten Profile.
   * @throws Exception
   */
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
