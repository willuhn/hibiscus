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

import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

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
    
    Assert.assertTrue(Arrays.equals(new String[]{"123456789012345678901234567",
                                                 "890123456789012345678901234",
                                                 "567890d12345678901234567890",
                                                 "1234567"},
                                    result));
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
    Assert.assertEquals("SVWZ falsch","Ein komischer.Verwendungszweck auf mehreren Zeilen Hier kommt nochwas",map.get(Tag.SVWZ));
    Assert.assertEquals("ABWA falsch","Das ist ein..Test//Text mit Zeilen-Umbruch",map.get(Tag.ABWA));
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
    Assert.assertEquals("SVWZ falsch","Das folgende Tag gibts nichtFoooABCD+Gehoert zum Verwendungszweck",map.get(Tag.SVWZ));
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
    Assert.assertEquals("SVWZ falsch","Das folgende Tag gibts nichtFooo ABCD+Gehoert zum Verwendungszweck",map.get(Tag.SVWZ));
    Assert.assertEquals("EREF falsch","Aber hier kommt noch was",map.get(Tag.EREF));
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
    Assert.assertEquals("SVWZ falsch","Das folgende Tag gibts nichtFooo ABCD+ Leerzeichen hinter dem Tag stoeren nicht",map.get(Tag.SVWZ));
    Assert.assertEquals("KREF falsch","und hier stoeren sie auch nicht, sind aber nicht teil des Value",map.get(Tag.KREF));
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
    Assert.assertEquals("SVWZ falsch","Nur eine Zeile",map.get(Tag.SVWZ));
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
    Assert.assertEquals("SVWZ falsch","",map.get(Tag.SVWZ));
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
    Assert.assertEquals("Map falsch",0,map.size());
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
    Assert.assertEquals("SVWZ falsch","Das ist eine Zeile ohne Tag Fooo",map.get(Tag.SVWZ));
    Assert.assertEquals("KREF falsch","Und hier kommen ploetzlich noch Tags. Der Teil bis zum ersten Tag ist dann eigentlich der Verwendungszweck",map.get(Tag.KREF));
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
    Assert.assertEquals("SVWZ falsch","Wir koennen auchmit",map.get(Tag.SVWZ));
    Assert.assertEquals("KREF falsch","Doppelpunkt als Separatur umgehen",map.get(Tag.KREF));
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
    Assert.assertEquals("SVWZ falsch","Das geht sogar gemischt IBAN: DE1234567890",map.get(Tag.SVWZ));
    Assert.assertEquals("IBAN falsch","DE1234567890",map.get(Tag.IBAN));
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
    Assert.assertEquals("IBAN falsch","DE49390500000000012345",map.get(Tag.IBAN));
    Assert.assertEquals("BIC falsch","AACSDE33",map.get(Tag.BIC));
    Assert.assertEquals("ABWA falsch","NetAachen",map.get(Tag.ABWA));
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
    Assert.assertEquals("IBAN falsch","DE12345678901234567890",map.get(Tag.IBAN));
    Assert.assertEquals("BIC falsch","GENODED1SAM",map.get(Tag.BIC));
    Assert.assertEquals("SVWZ","BIC:GENODED1SAM IBAN:DE12345678901234567890 Datum: 14.01.16 Zeit: 08:00 KD 00012345 TAN 12345 Beleg 12345  Kunde 12345",map.get(Tag.SVWZ));
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
    Assert.assertEquals("SVWZ falsch","Verwendungszweck",map.get(Tag.SVWZ));
    Assert.assertEquals("EREF falsch","1234567890123456789",map.get(Tag.EREF)); // Hier bleibt das Leerzeichen drin, da wir nicht wissen, ob es drin sein darf
    Assert.assertEquals("IBAN falsch","DE12345678901234567890",map.get(Tag.IBAN)); // Hier muss das Leerzeichen raus
    Assert.assertEquals("BIC falsch" ,"ABCDEFGH",map.get(Tag.BIC)); // Hier auch ohne Leerzeichen
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
    Assert.assertEquals("SVWZ falsch","Das ist Zeile 123",map.get(Tag.SVWZ));
  }
}
