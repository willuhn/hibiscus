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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.I18N;

/**
 * Bean mit moeglichen Zeitraeumen.
 */
public abstract class Range
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static Settings settings = new Settings(Range.class);
  
  private final static Range ALL    = new All();
  private final static Range D_7    = new LastSevenDays();
  private final static Range D_30   = new LastThirtyDays();
  private final static Range D_90   = new LastNinetyDays();
  private final static Range LY_1   = new Last365Days();
  private final static Range LY_3   = new Last3Years();
  private final static Range LY_5   = new Last5Years();
  private final static Range LY_10   = new Last10Years();
  
  private final static Range W_THIS = new ThisWeek();
  private final static Range W_LAST = new LastWeek();
  private final static Range W_2LAS = new SecondLastWeek();

  private final static Range M_THIS = new ThisMonth();
  private final static Range M_LAST = new LastMonth();
  private final static Range M_2LAS = new SecondLastMonth();
  private final static Range M_12   = new Last12Months();
  
  private final static Range Q_THIS = new ThisQuarter();
  private final static Range Q_LAST = new LastQuarter();
  private final static Range Q_2LAS = new SecondLastQuarter();
  
  private final static Range Y_THIS = new ThisYear();
  private final static Range Y_LAST = new LastYear();
  private final static Range Y_2LAS = new SecondLastYear();

  /**
   * Parameterpräfix für Zahlungverkehrs-Zeiträume
   * */
  public final static String CATEGORY_ZAHLUNGSVERKEHR = "zahlungsverkehr";
  /**
   * Parameterpräfix für Auswertungs-Zeiträume
   * */
  public final static String CATEGORY_AUSWERTUNG = "auswertung";

  /**
   * Bekannte Zeitraeume.
   */
  public final static List<Range> KNOWN = Arrays.asList(
    D_7,
    D_30,
    D_90,
    LY_1,
    LY_3,
    LY_5,
    LY_10,
    W_THIS,
    W_LAST,
    W_2LAS,
    M_THIS,
    M_LAST,
    M_2LAS,
    M_12,
    Q_THIS,
    Q_LAST,
    Q_2LAS,
    Y_THIS,
    Y_LAST,
    Y_2LAS,
    ALL
  );

  /**
   * Die Default-Auswahl der aktiven Zeitraeume.
   */
  private final static List<Range> DEFAULT = Arrays.asList(
      D_7,
      D_30,
      D_90,
      W_THIS,
      W_LAST,
      W_2LAS,
      M_THIS,
      M_LAST,
      M_2LAS,
      M_12,
      Q_THIS,
      Q_LAST,
      Q_2LAS,
      Y_THIS,
      Y_LAST,
      Y_2LAS,
      ALL
    );

  /**
   * Liefert die aktiven Zeitraeume fuer die angegebene Kategorie.
   * @param category Kategorie (sinnvollerweise CATEGORY_ZAHLUNGSVERKEHR oder CATEGORY_AUSWERTUNG)
   * @return Liste der anzuzeigenden Zeiträume für die gegebene Kategorie. 
   * */
  public final static List<Range> getActiveRanges(final String category)
  {
    final List<Range> result = new ArrayList<Range>();
    for (Range range : KNOWN)
    {
      if (settings.getBoolean(category + "." + range.getId(), DEFAULT.contains(range)))
        result.add(range);
    }
    return result;
  }
  
  /**
   * Speichert die fuer die Kategorie zu verwendenden Zeitraeume.
   * @param category Kategorie (sinnvollerweise CATEGORY_ZAHLUNGSVERKEHR oder CATEGORY_AUSWERTUNG)
   * @param ranges Liste der anzuzeigenden Zeiträume für die gegebene Kategorie. 
   */
  public final static void setActiveRanges(final String category, List<Range> ranges)
  {
    for (Range range : KNOWN)
    {
      settings.setAttribute(category + "." + range.getId(), ranges.contains(range));
    }
  }
  
  /**
   * Setzte die aktiven Zeitraeume auf die System-Vorgabe zurueck.
   * @param category Kategorie (sinnvollerweise CATEGORY_ZAHLUNGSVERKEHR oder CATEGORY_AUSWERTUNG)
   */
  public final static void resetActiveRanges(final String category)
  {
    for (Range range : KNOWN)
    {
      settings.setAttribute(category + "." + range.getId(), DEFAULT.contains(range));
    }
  }


  /**
   * Versucht den Range anhand des Identifiers zu ermitteln.
   * @param name der Name des Range.
   * @return der Range oder NULL, wenn er nicht gefunden wurde.
   */
  public static Range byId(String name)
  {
    if (name == null)
      return null;
    
    for (Range r:KNOWN)
    {
      if (r.getId().equals(name))
        return r;
    }
    
    return null;
  }
  
  /**
   * Berechnet das Start-Datum.
   * @return das Start-Datum.
   */
  public abstract Date getStart();
  
  /**
   * Berechnet das End-Datum.
   * @return das End-Datum.
   */
  public abstract Date getEnd();
  
  /**
   * Liefert einen Identifier fuer den Range.
   * @return Identifier fuer den Range.
   */
  public String getId()
  {
    return this.getClass().getSimpleName();
  }
  
  /**
   * Erzeugt einen neuen Kalender, der als Basis fuer die Berechnung dient.
   * @return einen neuen Kalender, der als Basis fuer die Berechnung dient.
   */
  protected Calendar createCalendar()
  {
    return Calendar.getInstance(Locale.GERMANY);
  }
  
  /**
   * Berechnet diese Woche.
   */
  public static class ThisWeek extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Woche: Diese");
    }
  }

  /**
   * Zeitraum ohne Einschränkungen
   **/
  public static class All extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      return null;
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      return null;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return i18n.tr("Alles");
    }
  }

  /**
   * Zeitraum fuer die letzten 7 Tage.
   */
  public static class LastSevenDays extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.DATE,-7);
      Date d = cal.getTime();
      return DateUtil.startOfDay(d);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      Date d = cal.getTime();
      return DateUtil.endOfDay(d);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return i18n.tr("Letzte 7 Tage");
    }
  }

  /**
   * Zeitraum fuer die letzten 30 Tage.
   */
  public static class LastThirtyDays extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.DATE,-30);
      Date d = cal.getTime();
      return DateUtil.startOfDay(d);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      Date d = cal.getTime();
      return DateUtil.endOfDay(d);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return i18n.tr("Letzte 30 Tage");
    }
  }

  /**
   * Zeitraum fuer die letzten 90 Tage.
   */
  public static class LastNinetyDays extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.DATE,-90);
      Date d = cal.getTime();
      return DateUtil.startOfDay(d);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      Date d = cal.getTime();
      return DateUtil.endOfDay(d);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return i18n.tr("Letzte 90 Tage");
    }
  }

  /**
   * Abstrakte Basis-Implementierung der Jahres-Zeitraeume.
   */
  private abstract static class LastYears extends Range
  {
    private String text;
    private int years;

    protected LastYears(int years, String text)
    {
      this.years = years;
      this.text = text;
    }

    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.YEAR,-years);
      cal.add(Calendar.DATE, 1);
      Date d = cal.getTime();
      return DateUtil.startOfDay(d);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      Date d = cal.getTime();
      return DateUtil.endOfDay(d);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return i18n.tr(text);
    }
  }

  /**
   * Zeitraum ab heute vor einem Jahr
   * */
  public static class Last365Days extends LastYears
  {
    protected Last365Days()
    {
       super(1, "Letzte 365 Tage");
    }
  }

  /**
   * Zeitraum ab heute vor drei Jahren
   * */
  public static class Last3Years extends LastYears{
    protected Last3Years()
    {
       super(3, "Letzte 3 Jahre");
    }
  }

  /**
   * Zeitraum ab heute vor fünf Jahren
   * */
  public static class Last5Years extends LastYears{
    protected Last5Years()
    {
       super(5, "Letzte 5 Jahre");
    }
  }

  /**
   * Zeitraum ab heute vor zehn Jahren
   * */
  public static class Last10Years extends LastYears{
    protected Last10Years()
    {
       super(10, "Letzte 10 Jahre");
    }
  }

  /**
   * Berechnet letzte Woche.
   */
  public static class LastWeek extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(new ThisWeek().getStart());
      cal.add(Calendar.WEEK_OF_YEAR,-1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.add(Calendar.DATE,6);
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Woche: Letzte");
    }
  }

  /**
   * Berechnet vorletzte Woche.
   */
  public static class SecondLastWeek extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(new ThisWeek().getStart());
      cal.add(Calendar.WEEK_OF_YEAR,-2);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.add(Calendar.DATE,6);
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Woche: Vorletzte");
    }
  }

  /**
   * Berechnet diesen Monat.
   */
  public static class ThisMonth extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Monat: Dieser");
    }
  }
  
  /**
   * Berechnet den letzten Monat.
   */
  public static class LastMonth extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.MONTH,-1);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Monat: Letzter");
    }
  }
  
  /**
   * Berechnet den vorletzten Monat.
   */
  public static class SecondLastMonth extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.MONTH,-2);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Monat: Vorletzter");
    }
  }

  /**
   * Berechnet den Zeitraum der letzten 12 Monate.
   */
  public static class Last12Months extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.MONTH,-12);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Monat: Letzte 12");
    }
  }

  /**
   * Berechnet dieses Quartal.
   */
  public static class ThisQuarter extends Range
  {
    private final static int[] quarters = {0, 0, 0, 3, 3, 3, 6, 6, 6, 9, 9, 9};
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.MONTH,quarters[cal.get(Calendar.MONTH)]);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.add(Calendar.MONTH,2);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Quartal: Dieses");
    }
  }

  /**
   * Berechnet letztes Quartal.
   */
  public static class LastQuarter extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(new ThisQuarter().getStart());
      cal.add(Calendar.MONTH,-3);
      return cal.getTime();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.add(Calendar.MONTH,2);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Quartal: Letztes");
    }
  }

  /**
   * Berechnet vorletztes Quartal.
   */
  public static class SecondLastQuarter extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(new LastQuarter().getStart());
      cal.add(Calendar.MONTH,-3);
      return cal.getTime();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.setTime(this.getStart());
      cal.add(Calendar.MONTH,2);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Quartal: Vorletztes");
    }
  }

  /**
   * Berechnet dieses Jahr.
   */
  public static class ThisYear extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.MONTH,Calendar.JANUARY);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.set(Calendar.MONTH,Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Jahr: Dieses");
    }
  }

  /**
   * Berechnet letztes Jahr.
   */
  public static class LastYear extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.YEAR,-1);
      cal.set(Calendar.MONTH,Calendar.JANUARY);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.YEAR,-1);
      cal.set(Calendar.MONTH,Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Jahr: Letztes");
    }
  }

  /**
   * Berechnet vorletztes Jahr.
   */
  public static class SecondLastYear extends Range
  {
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    public Date getStart()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.YEAR,-2);
      cal.set(Calendar.MONTH,Calendar.JANUARY);
      cal.set(Calendar.DAY_OF_MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    public Date getEnd()
    {
      Calendar cal = this.createCalendar();
      cal.add(Calendar.YEAR,-2);
      cal.set(Calendar.MONTH,Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Jahr: Vorletztes");
    }
  }
}


