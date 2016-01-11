/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
        "SVWZ+Ein komischer.Verwendungszweck",
        "auf mehreren Zeilen",
        "Hier kommt nochwas",
        "ABWA+Das ist ein..Test//Text",
        "mit Zeilen-Umbruch"
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    Assert.assertEquals("SVWZ falsch","Ein komischer.Verwendungszweckauf mehreren ZeilenHier kommt nochwas",map.get(Tag.SVWZ));
    Assert.assertEquals("ABWA falsch","Das ist ein..Test//Textmit Zeilen-Umbruch",map.get(Tag.ABWA));
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
        "Fooo",
        "ABCD+Gehoert zum Verwendungszweck",
        "EREF+Aber hier kommt noch was"
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    Assert.assertEquals("SVWZ falsch","Das folgende Tag gibts nichtFoooABCD+Gehoert zum Verwendungszweck",map.get(Tag.SVWZ));
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
        "Fooo",
        "ABCD+ Leerzeichen hinter dem Tag stoeren nicht",
        "KREF+ und hier stoeren sie auch nicht, sind aber nicht teil des Value "
    };
    
    Map<Tag,String> map = VerwendungszweckUtil.parse(test);
    Assert.assertEquals("SVWZ falsch","Das folgende Tag gibts nichtFoooABCD+ Leerzeichen hinter dem Tag stoeren nicht",map.get(Tag.SVWZ));
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
}


