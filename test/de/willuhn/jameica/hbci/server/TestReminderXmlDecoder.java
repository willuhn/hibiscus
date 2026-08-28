/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderInterval.TimeUnit;

/**
 * Tests the data-only reminder decoder.
 */
public class TestReminderXmlDecoder
{
  /**
   * Existing XMLEncoder reminder data remains readable.
   */
  @Test
  void decodeReminder() throws Exception
  {
    Reminder expected = new Reminder();
    expected.setQueue("test.queue");
    expected.setDate(new Date(1234567890000L));
    expected.setEnd(new Date(1234567990000L));
    expected.setReminderInterval(new ReminderInterval(TimeUnit.WEEKS,2));
    expected.setData("text","hello");
    expected.setData("date",new Date(123L));
    expected.setData("boolean",Boolean.TRUE);
    expected.setData("byte",Byte.valueOf((byte)1));
    expected.setData("short",Short.valueOf((short)2));
    expected.setData("count",Integer.valueOf(7));
    expected.setData("long",Long.valueOf(8L));
    expected.setData("float",Float.valueOf(1.5f));
    expected.setData("double",Double.valueOf(2.5d));
    expected.setData("null",null);

    Reminder actual = ReminderXmlDecoder.decode(encode(expected));
    Assertions.assertEquals(expected.getQueue(),actual.getQueue());
    Assertions.assertEquals(expected.getDate(),actual.getDate());
    Assertions.assertEquals(expected.getEnd(),actual.getEnd());
    Assertions.assertEquals(expected.getReminderInterval().getTimeUnit(),actual.getReminderInterval().getTimeUnit());
    Assertions.assertEquals(expected.getReminderInterval().getInterval(),actual.getReminderInterval().getInterval());
    Assertions.assertEquals(expected.getData(),actual.getData());
  }

  /**
   * References emitted by XMLEncoder for a shared date remain readable.
   */
  @Test
  void decodeSharedDateReference() throws Exception
  {
    Date shared = new Date(123L);
    Reminder expected = new Reminder();
    expected.setDate(shared);
    expected.setEnd(shared);
    expected.setData("date",shared);

    Reminder actual = ReminderXmlDecoder.decode(encode(expected));
    Assertions.assertSame(actual.getDate(),actual.getEnd());
    Assertions.assertSame(actual.getDate(),actual.getData("date"));
  }

  /**
   * XMLDecoder instructions must be rejected without executing their side effects.
   */
  @Test
  void rejectExecutableXml(@TempDir Path directory) throws Exception
  {
    Path proof = directory.resolve("proof.txt");
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<java version=\"21\" class=\"java.beans.XMLDecoder\">\n"
        + "  <object class=\"java.io.PrintWriter\">\n"
        + "    <string>" + proof + "</string>\n"
        + "    <void method=\"println\"><string>executed</string></void>\n"
        + "    <void method=\"close\"/>\n"
        + "  </object>\n"
        + "</java>\n";

    Assertions.assertThrows(IOException.class,() -> ReminderXmlDecoder.decode(xml));
    Assertions.assertFalse(Files.exists(proof));
  }

  /**
   * External resources must be rejected before they can be resolved.
   */
  @Test
  void rejectDoctype(@TempDir Path directory) throws Exception
  {
    Path secret = directory.resolve("secret.txt");
    Files.writeString(secret,"EXTERNAL_ENTITY_EXPANDED");
    String xml = "<!DOCTYPE java [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>\n"
        + "<java version=\"21\" class=\"java.beans.XMLDecoder\">\n"
        + "  <object class=\"de.willuhn.jameica.reminder.Reminder\">\n"
        + "    <void property=\"queue\"><string>&xxe;</string></void>\n"
        + "  </object>\n"
        + "</java>\n";

    Assertions.assertThrows(SAXParseException.class,() -> ReminderXmlDecoder.decode(xml));
  }

  /**
   * Method calls and unknown properties are not part of the data format.
   */
  @Test
  void rejectUnknownInstructions()
  {
    String xml = "<java version=\"21\" class=\"java.beans.XMLDecoder\">\n"
        + "  <object class=\"de.willuhn.jameica.reminder.Reminder\">\n"
        + "    <void method=\"getClass\"/>\n"
        + "  </object>\n"
        + "</java>\n";

    Assertions.assertThrows(IOException.class,() -> ReminderXmlDecoder.decode(xml));
  }

  /**
   * References must point to the single previously defined reminder data map.
   */
  @Test
  void rejectInvalidReference()
  {
    String invalid = "<java version=\"21\" class=\"java.beans.XMLDecoder\">\n"
        + "  <object class=\"de.willuhn.jameica.reminder.Reminder\">\n"
        + "    <void property=\"data\"><object idref=\"missing\"/></void>\n"
        + "  </object>\n"
        + "</java>\n";
    String duplicate = "<java version=\"21\" class=\"java.beans.XMLDecoder\">\n"
        + "  <object class=\"de.willuhn.jameica.reminder.Reminder\">\n"
        + "    <void id=\"data\" property=\"data\"/>\n"
        + "    <void property=\"data\"><object idref=\"data\"/></void>\n"
        + "    <void property=\"data\"><object idref=\"data\"/></void>\n"
        + "  </object>\n"
        + "</java>\n";

    Assertions.assertThrows(IOException.class,() -> ReminderXmlDecoder.decode(invalid));
    Assertions.assertThrows(IOException.class,() -> ReminderXmlDecoder.decode(duplicate));
  }

  private static String encode(Reminder reminder) throws Exception
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (XMLEncoder encoder = new XMLEncoder(bytes))
    {
      encoder.writeObject(reminder);
    }
    return bytes.toString(StandardCharsets.UTF_8.name());
  }
}
