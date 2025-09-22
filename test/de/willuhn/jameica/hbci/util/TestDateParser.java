/**********************************************************************
 *
 * Copyright (c) 2025 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.util;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * Testet den Date-Parser.
 */
public class TestDateParser
{
  /**
   * Testet das Format dd.MM.yy
   * @throws Exception
   */
  @Test
  public void testddMMyy() throws Exception
  {
    this.test("14.05.28","14.05.2028");
    this.test("14.5.28","14.05.2028");
    this.test("4.05.28","04.05.2028");
    this.test("4.5.28","04.05.2028");
  }

  /**
   * Testet das Format dd.MM.yy
   * @throws Exception
   */
  @Test
  public void testddMMyy3() throws Exception
  {
    this.test("14|05|28","14.05.2028");
    this.test("14|5|28","14.05.2028");
    this.test("4|05|28","04.05.2028");
    this.test("4|5|28","04.05.2028");
  }

  /**
   * Testet das Format MMMM yy
   * @throws Exception
   */
  @Test
  public void testMMMMyy() throws Exception
  {
    this.test("März 25","01.03.2025");
  }

  /**
   * Testet das Format MMM yy
   * @throws Exception
   */
  @Test
  public void testMMMyy() throws Exception
  {
    this.test("Jan 25","01.01.2025");
  }

  /**
   * Testet das Format MMM. yy
   * @throws Exception
   */
  @Test
  public void testMMMyy2() throws Exception
  {
    this.test("Jan. 24","01.01.2024");
  }

  /**
   * Testet das Format dd. MMM. yy
   * @throws Exception
   */
  @Test
  public void testddMMMyy() throws Exception
  {
    this.test("4. Jan.23","04.01.2023");
  }

  /**
   * Testet das Format dd. MMM yy
   * @throws Exception
   */
  @Test
  public void testddMMMyy2() throws Exception
  {
    this.test("23. Dez 25","23.12.2025");
  }

  /**
   * Testet das Format dd. MMMM yy
   * @throws Exception
   */
  @Test
  public void testddMMMMyy() throws Exception
  {
    this.test("14. Dezember 25","14.12.2025");
  }

  /**
   * Testet das Format dd.MM.yy
   * @throws Exception
   */
  @Test
  public void testddMMyy2() throws Exception
  {
    this.test("141225","14.12.2025");
  }
  
  /**
   * Testet das Format yyyy.MM.dd
   * @throws Exception
   */
  @Test
  public void testyyyyMMdd() throws Exception
  {
    this.test("2022.9.20","20.09.2022");
    this.test("2022.09.20","20.09.2022");
  }

  /**
   * Testet das Format MMMM yyyy
   * @throws Exception
   */
  @Test
  public void testMMMMyyyy() throws Exception
  {
    this.test("August 2025","01.08.2025");
  }

  /**
   * Testet das Format MMM yyyy
   * @throws Exception
   */
  @Test
  public void testMMMyyyy() throws Exception
  {
    this.test("Feb 2025","01.02.2025");
  }


  /**
   * Testet das Format MM/yyyy
   * @throws Exception
   */
  @Test
  public void testMMyyyy() throws Exception
  {
    this.test("11/2024","01.11.2024");
  }

  /**
   * Testet das Format MM.yyyy
   * @throws Exception
   */
  @Test
  public void testMMyyyy2() throws Exception
  {
    this.test("11.2024","01.11.2024");
  }

  /**
   * Testet das Format MMM. yyyy
   * @throws Exception
   */
  @Test
  public void testMMMyyyy2() throws Exception
  {
    this.test("Jun. 2025","01.06.2025");
  }
  
  /**
   * Testet das Format MMM/yyyy (Agenda-Gehaltsnachweise)
   * @throws Exception
   */
  @Test
  public void testMMMyyyy3() throws Exception
  {
    this.test("Jun/2025","01.06.2025");
  }


  /**
   * Testet das Format MMM/yyyy
   * @throws Exception
   */
  @Test
  public void testMMyy() throws Exception
  {
    this.test("05.25","01.05.2025");
  }
  
  /**
   * Testet das Format dd.MM.yyyy
   * @throws Exception
   */
  @Test
  public void testddMMyyyy() throws Exception
  {
    this.test("3.9.2025","03.09.2025");
    this.test("03.9.2025","03.09.2025");
    this.test("3.09.2025","03.09.2025");
    this.test("03.09.2025","03.09.2025");
  }

  /**
   * Testet das Format dd. MMM. yyyy
   * @throws Exception
   */
  @Test
  public void testddMMMyyyy() throws Exception
  {
    this.test("3. Jan. 2025","03.01.2025");
  }

  /**
   * Testet das Format dd. MMM yyyy
   * @throws Exception
   */
  @Test
  public void testddMMMyyyy2() throws Exception
  {
    this.test("3. Jan 2025","03.01.2025");
  }

  /**
   * Testet das Format dd. MMMM yyyy
   * @throws Exception
   */
  @Test
  public void testddMMMMyyyy() throws Exception
  {
    this.test("9. April 2025","09.04.2025");
  }
  
  /**
   * Testet das Format ddMMyyyy
   * @throws Exception
   */
  @Test
  public void testddMMyyyy2() throws Exception
  {
    this.test("12082025","12.08.2025");
  }

  /**
   * Testet das Format yyyy/MM/dd
   * @throws Exception
   */
  @Test
  public void testyyyyMMdd2() throws Exception
  {
    this.test("2028/03/05","05.03.2028");
    this.test("2028/3/05","05.03.2028");
    this.test("2028/03/5","05.03.2028");
    this.test("2028/3/5","05.03.2028");
    this.test("2028/12/5","05.12.2028");
  }

  /**
   * Testet das Format yyyy-MM-dd
   * @throws Exception
   */
  @Test
  public void testyyyyMMdd3() throws Exception
  {
    this.test("2028-03-05","05.03.2028");
    this.test("2028-3-05","05.03.2028");
    this.test("2028-03-5","05.03.2028");
    this.test("2028-3-5","05.03.2028");
    this.test("2028-12-5","05.12.2028");
  }

  /**
   * Testet das Format MM/dd/yyyy
   * @throws Exception
   */
  @Test
  public void testMMddyyyy() throws Exception
  {
    this.test("03/05/2028","05.03.2028");
    this.test("03/5/2028","05.03.2028");
    this.test("3/05/2028","05.03.2028");
    this.test("3/5/2028","05.03.2028");
    this.test("12/5/2028","05.12.2028");
  }
  
  /**
   * Testet das Format MM/dd/yy
   * @throws Exception
   */
  @Test
  public void testMMddyy() throws Exception
  {
    this.test("03/05/28","05.03.2028");
    this.test("03/5/28","05.03.2028");
    this.test("3/05/28","05.03.2028");
    this.test("3/5/28","05.03.2028");
    this.test("12/5/28","05.12.2028");
  }

  /**
   * Stellt sicher, dass Jahre ausserhalb des Gueltigkeitsbereiches als ungueltig erkannt werden.
   * Wir nehmen hier extra ein Pattern, bei dem vorher nicht mit Regex gecheckt wird, um
   * sicherzustellen, dass wir ueberhaupt an dem Range-Check ankommen.
   * @throws Exception
   */
  @Test
  public void testRange() throws Exception
  {
    Assert.assertNull(DateParser.parse("Januar 1890"));
    Assert.assertNull(DateParser.parse("Dezember 2140"));
  }
  
  /**
   * Testet die Formate dd-MM-yy und dd-MM-yyyy
   * @throws Exception
   */
  @Test
  public void testddMMyy4() throws Exception
  {
    this.test(" 13-10-97 ", "13.10.1997");
    this.test("10-10-70", "10.10.1970");
    this.test("1-10-70", "01.10.1970");
    this.test("31-1-05", "31.01.2005");

    this.test(" 13-10-1997 ", "13.10.1997");
    this.test("10-10-1970", "10.10.1970");
    this.test("1-10-1970", "01.10.1970");
    this.test("31-1-2030", "31.01.2030");
    this.test("31-1-1930", "31.01.1930");
  }
  
  /**
   * Testet Datumsangaben mit unvollstaendigen Trennzeichen
   * @throws Exception
   */
  @Test
  public void testMissingSeparators() throws Exception
  {
    this.test("0106,17", "01.06.2017");
    this.test("2406.70", "24.06.1970");
    this.test("01,0915", "01.09.2015");
    this.test("2408.2009", "24.08.2009");
    this.test("01.112011", "01.11.2011");
    this.test("1708.1985", "17.08.1985");
  }

  /**
   * @param date
   * @param expected
   * @throws Exception
   */
  private void test(String date, String expected) throws Exception
  {
    final Date actual = DateParser.parse(date);
    Assert.assertNotNull("Datum war NULL",actual);

    Date d = new SimpleDateFormat("dd.MM.yyyy").parse(expected);
    Assert.assertEquals(d,actual);
  }
}
