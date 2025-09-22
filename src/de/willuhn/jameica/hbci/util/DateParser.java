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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.util.DateParser.DatePattern.Flag;
import de.willuhn.logging.Logger;

/**
 * Extra-toleranter Parser für Datumswerte.
 */
public class DateParser
{
  private final static int YEARS_MAX_PAST = 100;
  private final static int YEARS_MAX_FUTURE = 100;
  
  // WITCHTIG: Die Reihenfolge der Patterns ist wichtig (insbs. auch yy vor yyyy, da sonst zweistellige Jahreszahlen, beispielsweise 14 als 0014 inderpretiert werden)
  private final static List<DatePattern> PATTERNS = Arrays.asList(
      
    // zweistellige Jahreszahlen
    new DatePattern("dd.MM.yy","(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("dd|MM|yy","(0?[1-9]|[12][0-9]|3[01])\\|(0?[1-9]|1[012])\\|\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("dd-MM-yy","(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("MMMM yy",null,Flag.NO_DAY),
    new DatePattern("MMM yy",null,Flag.NO_DAY),
    new DatePattern("MMM.yy",null,Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("MM.yy","(0?[1-9]|1[012])\\.\\d{2}",Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("dd.MMM.yy",null,Flag.NO_WHITESPACE),
    new DatePattern("dd.MMMM.yy",null,Flag.NO_WHITESPACE),
    new DatePattern("ddMMyy","(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[012])\\d{2}",Flag.NO_WHITESPACE),

    // vierstellige Jahreszahlen
    new DatePattern("yyyy.MM.dd","(19|20)\\d{2}\\.(0?[1-9]|1[012])\\.(0?[1-9]|[12][0-9]|3[01])",Flag.NO_WHITESPACE),
    new DatePattern("yyyy/MM/dd","(19|20)\\d{2}/(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])",Flag.NO_WHITESPACE),
    new DatePattern("yyyy-MM-dd","(19|20)\\d{2}-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])",Flag.NO_WHITESPACE),

    // Daten ohne Tag
    new DatePattern("MMMM yyyy",null,Flag.NO_DAY),
    new DatePattern("MMM yyyy",null,Flag.NO_DAY),
    new DatePattern("MMM.yyyy",null,Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("MM/yyyy","(0?[1-9]|1[012])/(19|20)\\d{2}",Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("MM.yyyy","(0?[1-9]|1[012])\\.(19|20)\\d{2}",Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("MMM/yyyy",null,Flag.NO_DAY,Flag.NO_WHITESPACE),
    new DatePattern("MMMM/yyyy",null,Flag.NO_DAY,Flag.NO_WHITESPACE),
    
    // Typische deutsche Schreibweisen
    new DatePattern("dd.MM.yyyy","(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.(19|20)\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("dd-MM-yyyy","(0?[1-9]|[12][0-9]|3[01])-(0?[1-9]|1[012])-(19|20)\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("ddMMyyyy","(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[012])(19|20)\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("dd.MMM.yyyy",null,Flag.NO_WHITESPACE),
    new DatePattern("dd.MMM.yyyy",null,Flag.NO_WHITESPACE),
    new DatePattern("dd.MMMM.yyyy",null,Flag.NO_WHITESPACE),

    // In USA gebraeuchlich
    new DatePattern("MM/dd/yyyy","(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(19|20)\\d{2}",Flag.NO_WHITESPACE),
    new DatePattern("MM/dd/yy","(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/\\d{2}",Flag.NO_WHITESPACE)
  );
  
  private static DateFormatSymbols symbols = null;

  static
  {
    symbols = new DateFormatSymbols(Locale.GERMAN);
    
    // Seit Java 9 werden die Monate und Tage in der Kurzform mit einem extra "." am Ende ausgegeben.
    // Das passiert aber nur im deutschen Locale. Da niemand sowas wie "03.Jan..2019" schreibt,
    // entfernen wir die Punkte aus den Abkuerzungen.
    // Ausserdem werden die Namen von kurzen Monaten gar nicht mehr abgekuerzt. Die Kurzform von "Juni" ist
    // also weder "Jun." noch "Jun" sondern "Juni".
    List<String> shortMonths = Arrays.asList(symbols.getShortMonths());
    List<String> fixedMonths = new ArrayList<String>();
    for (String s:shortMonths)
    {
      if (s.endsWith("."))
        s = s.substring(0,s.length()-1);
      
      if (s.length() > 3)
        s = s.substring(0,3);
      fixedMonths.add(s);
    }
    symbols.setShortMonths(fixedMonths.toArray(new String[fixedMonths.size()]));

    List<String> shortDays = Arrays.asList(symbols.getShortWeekdays());
    shortDays.replaceAll((s) -> {return s.replace(".","");});
    symbols.setShortWeekdays(shortDays.toArray(new String[shortDays.size()]));
  }
  
  /**
   * Parst einen gegebenen Datumsstring fehlertolerant.
   * @param date der zu parsende String.
   * @return das Datum oder NULL, wenn es nicht geparst werden konnte.
   */
  public static Date parse(String date)
  {
    date = StringUtils.trimToNull(date);
    
    if (date == null)
      return null;

    // Whitespaces und Sonderzeichen am Anfang und Ende entfernen.
    // Aber nur maximal 3 Zeichen. Wenn es mehr sind, stimmt etwas nicht.
    date = date.replaceAll("^[\\p{Punct}]{1,3}","");
    date = date.replaceAll("[\\p{Punct}]{1,3}$","");
    
    // Umlaute ersetzen
    date = date.replaceAll("(a|A)(e|E)", "ä");
    date = date.replaceAll("(u|U)(e|E)", "ü");
    date = date.replaceAll("(o|O)(e|E)", "ö");

    // Wenn das Datum Punkte enthaelt, dann alle anderen Zeichen, die so
    // aehnlich aussehen, gegen Punkte ersetzen. Auch Leerzeichen. Anschliessend alle
    // mehrfach aufeinander folgenden Punkte gegen jeweils einen ersetzen.
    if (date.contains("."))
      date = date.replaceAll("[- ;]",".");
    
    // Mehrfach aufeinander folgende Punkte gegen einen ersetzen. Aber maximal 8.
    date = date.replaceAll("[\\.]{2,8}","."); 

    // Leerzeichen zwischen zwei Ziffern entfernen.
    date = date.replaceAll("(\\d) +(\\d)","$1$2");
    
    for (DatePattern dp:PATTERNS)
    {
      try
      {
        String toParse = date;
        
        if (dp.hasFlag(Flag.NO_WHITESPACE))
          toParse = StringUtils.deleteWhitespace(toParse);
        
        final Date d = internalParse(toParse,dp);
        
        if (d != null)
        {
          Logger.debug("date " + date + " matched to pattern " + dp.pattern);
          return d;
        }
      }
      catch (Exception e)
      {
        // ignorieren und nächstes Format testen
      }
    }
    
    // Fallback: wenn nur ein einzelnes Trennzeichen enthalten ist, entfernen wir es komplett
    // Aber nur, wenn es mehr als 5 Zeichen sind (sonst kollidiert es mit "mm.yy" und wenn keine Buchstaben enthalten sind, sonst kollidiert es mit "dd. MMM yy")
    final String tmp = date.replaceAll("[\\.,;-]","");
    if (tmp.length() > 5 && tmp.matches("^[\\d]*$") && tmp.length() == date.length() - 1)
      return parse(tmp);

    Logger.debug("unparsable date: " + date);
    return null;
  }
  
  /**
   * Parst ein Datum im angegebenen Format.
   * @param date das Datum.
   * @param format das Format.
   * @return das Datum oder NULL, wenn es nicht geparst werden konnte.
   */
  private static Date internalParse(String date, DatePattern format)
  {
    if (date == null)
      return null;
    
    // Checken, ob der Regex passt, wenn einer angegeben ist.
    if (format.regex != null && !date.matches(format.regex))
      return null;
    
    final SimpleDateFormat df = new SimpleDateFormat(format.pattern,symbols);

    try
    {
      Date result = df.parse(date);
      
      if (format.hasFlag(Flag.NO_DAY))
      {
        // Wenn kein Tag angegeben ist, nehmen wir den 1. des Monats
        final Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        cal.set(Calendar.DATE, 1);
        result = cal.getTime();
      }
      
      // Jetzt schecken wir noch, ob sich das Jahr in einem plausiblen Bereich bewegt.
      final Calendar cal = Calendar.getInstance();
      cal.setTime(result);
      int year = cal.get(Calendar.YEAR);

      cal.setTime(new Date());
      cal.add(Calendar.YEAR,-YEARS_MAX_PAST);
      int yearMin = cal.get(Calendar.YEAR);
      
      cal.setTime(new Date());
      cal.add(Calendar.YEAR,YEARS_MAX_FUTURE);
      int yearMax = cal.get(Calendar.YEAR);
      
      // Ausserhalb des gültigen Bereichs.
      if (year < yearMin || year > yearMax)
        return null;
      
      return result;
    }
    catch (Exception e)
    {
      return null;
    }
  }
  
  /**
   * Hilfsklasse, welche das Datumsformat mit optionalen Parametern haelt.
   */
  public static class DatePattern
  {
    /**
     * Die Flags.
     */
    public static enum Flag
    {
      /**
       * Datum hat keines Tages-Angabe.
       */
      NO_DAY,
      
      /**
       * Alle Whitespaces entfernen - auch mittendrin.
       */
      NO_WHITESPACE,
    }

    private String pattern = null;
    private String regex = null;
    private Flag[] flags;
    
     /**
     * ct.
     * @param pattern
     * @param dayOffset
     * @param die Flags.
     * @param regex zusaetzlicher Regex, auf den das Datum passen muss, damit es matcht.
     */
    private DatePattern(String pattern, String regex, Flag... flags)
    {
      this.pattern = pattern;
      this.regex = regex;
      this.flags = flags;
    }
    
    /**
     * Prueft, ob das Date-Pattern das angegebene Flag hat.
     * @param flag das zu pruefende Flag.
     * @return true, wenn es das Flag hat.
     */
    private boolean hasFlag(Flag flag)
    {
      if (this.flags == null || this.flags.length == 0)
        return false;
      
      for (Flag f:this.flags)
      {
        if (f != null && f.equals(flag))
          return true;
      }
      
      return false;
    }
    
    /**
     * Liefert das Datumsformat.
     * @return das Datumsformat.
     */
    public String getPattern()
    {
      return this.pattern;
    }
  }
}
