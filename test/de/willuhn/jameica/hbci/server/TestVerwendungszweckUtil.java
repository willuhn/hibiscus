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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;

/**
 * Testet die Klasse "VerwendungszweckUtil".
 */
public class TestVerwendungszweckUtil
{
  /**
   * Testet die Funktion" rewrap".
   * @throws Exception
   */
  @Test
  public void testRewrap() throws Exception
  {
    String[] test =
    {
      "123456789012345678901234567890",
      "123456789012345678901234567890 ",
      null,
      "d",
      " ",
      "123456789012345678901234567"
    };
    
    String[] result = VerwendungszweckUtil.rewrap(27,test);
    
    assertArrayEquals(new String[]{"123456789012345678901234567",
                                   "890123456789012345678901234",
                                   "567890d12345678901234567890",
                                   "1234567"},
                                    result);
  }
  
  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse001() throws Exception
  {
    String [] test =
    {
        "SVWZ+Ein komischer.Verwendungszweck ",
        "auf mehreren Zeilen ",
        "Hier kommt nochwas",
        "ABWA+Das ist ein..Test//Text ",
        "mit Zeilen-Umbruch"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Ein komischer.Verwendungszweck auf mehreren Zeilen Hier kommt nochwas",map.get(Tag.SVWZ), "SVWZ falsch");
    assertEquals("Das ist ein..Test//Text mit Zeilen-Umbruch",map.get(Tag.ABWA), "ABWA falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse002() throws Exception
  {
    String [] test =
    {
        "SVWZ+Das folgende Tag gibts nicht",
        "Fooo",
        "ABCD+Gehoert zum Verwendungszweck"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das folgende Tag gibts nichtFoooABCD+Gehoert zum Verwendungszweck",map.get(Tag.SVWZ), "SVWZ falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse003() throws Exception
  {
    String [] test =
    {
        "SVWZ+Das folgende Tag gibts nicht",
        "Fooo ",
        "ABCD+Gehoert zum Verwendungszweck",
        "EREF+Aber hier kommt noch was"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das folgende Tag gibts nichtFooo ABCD+Gehoert zum Verwendungszweck",map.get(Tag.SVWZ),"SVWZ falsch");
    assertEquals("Aber hier kommt noch was",map.get(Tag.EREF), "EREF falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse004() throws Exception
  {
    String [] test =
    {
        "SVWZ+Das folgende Tag gibts nicht",
        "Fooo ",
        "ABCD+ Leerzeichen hinter dem Tag stoeren nicht",
        "KREF+ und hier stoeren sie auch nicht, sind aber nicht teil des Value "
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das folgende Tag gibts nichtFooo ABCD+ Leerzeichen hinter dem Tag stoeren nicht",map.get(Tag.SVWZ), "SVWZ falsch");
    assertEquals("und hier stoeren sie auch nicht, sind aber nicht teil des Value",map.get(Tag.KREF), "KREF falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse005() throws Exception
  {
    String [] test =
    {
        "SVWZ+Nur eine Zeile"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Nur eine Zeile",map.get(Tag.SVWZ), "SVWZ falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse006() throws Exception
  {
    String [] test =
    {
        "SVWZ+"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("",map.get(Tag.SVWZ), "SVWZ falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse007() throws Exception
  {
    String [] test =
    {
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals(0, map.size(),"Map falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse008() throws Exception
  {
    String [] test =
    {
        "Das ist eine Zeile ohne Tag ",
        "Fooo",
        "KREF+Und hier kommen ploetzlich noch Tags. Der Teil bis zum ersten Tag ist dann eigentlich der Verwendungszweck"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das ist eine Zeile ohne Tag Fooo",map.get(Tag.SVWZ), "SVWZ falsch");
    assertEquals("Und hier kommen ploetzlich noch Tags. Der Teil bis zum ersten Tag ist dann eigentlich der Verwendungszweck",map.get(Tag.KREF),"KREF falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse009() throws Exception
  {
    String [] test =
    {
        "Wir koennen auch",
        "mit",
        "KREF: Doppelpunkt als Separatur umgehen"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Wir koennen auchmit",map.get(Tag.SVWZ),"SVWZ falsch");
    assertEquals("Doppelpunkt als Separatur umgehen",map.get(Tag.KREF), "KREF falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse010() throws Exception
  {
    String [] test =
    {
        "SVWZ+ Das geht sogar",
        " gemischt ",
        "IBAN: DE1234567890 "
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das geht sogar gemischt",map.get(Tag.SVWZ),"SVWZ falsch");
    assertEquals("DE1234567890",map.get(Tag.IBAN),"IBAN falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse011() throws Exception
  {
    String [] test =
    {
        "IBAN: DE49390500000000012345 BIC: AACSDE33 ABWA: NetAachen"
    };

    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("DE49390500000000012345",map.get(Tag.IBAN), "IBAN falsch");
    assertEquals("AACSDE33",map.get(Tag.BIC), "BIC falsch");
    assertEquals("NetAachen",map.get(Tag.ABWA), "ABWA falsch");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse012() throws Exception
  {
    String [] test =
    {
        "SVWZ+BIC:GENODED1SAM ",
        "IBAN:DE12345678901234567890 ",
        "Datum: 14.01.16 Zeit: 08:00 ",
        "KD 00012345 TAN 12345 ",
        "Beleg 12345  Kunde 12345"
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("DE12345678901234567890",map.get(Tag.IBAN),"IBAN falsch");
    assertEquals("GENODED1SAM",map.get(Tag.BIC), "BIC falsch");
    assertEquals("BIC:GENODED1SAM IBAN:DE12345678901234567890 Datum: 14.01.16 Zeit: 08:00 KD 00012345 TAN 12345 Beleg 12345  Kunde 12345",map.get(Tag.SVWZ),"SVWZ");
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse013() throws Exception
  {
    String [] test =
    {
        "Verwendungszweck EREF: 1234",
        "567890123456789 IBAN: DE123",
        "45678901234567890 BIC: ABCD",
        "EFGH"
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Verwendungszweck",map.get(Tag.SVWZ),"SVWZ falsch");
    assertEquals("1234567890123456789",map.get(Tag.EREF),"EREF falsch"); // Hier bleibt das Leerzeichen drin, da wir nicht wissen, ob es drin sein darf
    assertEquals("DE12345678901234567890",map.get(Tag.IBAN),"IBAN falsch"); // Hier muss das Leerzeichen raus
    assertEquals("ABCDEFGH",map.get(Tag.BIC),"BIC falsch"); // Hier auch ohne Leerzeichen
  }

  /**
   * Testet das Parsen der Tags.
   * @throws Exception
   */
  @Test
  public void testParse014() throws Exception
  {
    // Entfernen von Zeilenumbruechen im Verwendungszweck
    String [] test =
    {
        "SVWZ+Das ist Zeile 1",
        "2",
        "3"
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    assertEquals("Das ist Zeile 123",map.get(Tag.SVWZ),"SVWZ falsch");
  }
}


