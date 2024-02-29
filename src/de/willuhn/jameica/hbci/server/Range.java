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
import java.util.Objects;
import java.util.UUID;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
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
   * Enum mit den Kategorien.
   */
  public static enum Category
  {
    /**
     * Kategorie für Zahlungsverkehr.
     */
    ZAHLUNGSVERKEHR("zahlungsverkehr",i18n.tr("Zahlungsverkehr")),
    
    /**
     * Kategorie für Auswertungen.
     */
    AUSWERTUNG("auswertung",i18n.tr("Auswertungen, Umsatzlisten und Kontoauszüge")),
    
    ;
    
    private String id = null;
    private String name = null;
    
    /**
     * ct.
     * @param id
     * @param name
     */
    private Category(String id, String name)
    {
      this.id = id;
      this.name = name;
    }
    
    /**
     * Liefert die ID der Kategorie.
     * @return id die ID der Kategorie.
     */
    public String getId()
    {
      return id;
    }
    
    /**
     * Liefert den Namen der Kategorie.
     * @return der Name der Kategorie.
     */
    public String getName()
    {
      return name;
    }
  }

  /**
   * Bekannte Zeitraeume.
   */
  private final static List<Range> KNOWN = Arrays.asList(
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
   * Liefert alle Zeitraeme fuer die angegebene Kategorie.
   * @param category Kategorie.
   * @return Liste der Zeitraeume.
   */
  public static List<Range> getAllRanges(final Category category)
  {
    final List<Range> result = new ArrayList<Range>();
    result.addAll(getCustomRanges(category));
    result.addAll(KNOWN);
    
    return result;
  }
  
  /**
   * Liefert die aktiven Zeitraeume fuer die angegebene Kategorie.
   * @param category Kategorie.
   * @return Liste der anzuzeigenden Zeiträume für die gegebene Kategorie. 
   * */
  public static List<Range> getActiveRanges(final Category category)
  {
    final List<Range> result = new ArrayList<Range>();
    
    for (Range range:getCustomRanges(category))
    {
      if (settings.getBoolean(category.getId() + "." + range.getId(), true))
        result.add(range);
    }

    for (Range range : KNOWN)
    {
      if (settings.getBoolean(category.getId() + "." + range.getId(), DEFAULT.contains(range)))
        result.add(range);
    }
    
    return result;
  }
  
  /**
   * Liefert die Liste der benutzerspezifischen Zeiträume.
   * @param category Kategorie.
   * @return Liste der benutzerspezifischen Zeiträume. Nie NULL sondern höchstens eine leere Liste.
   */
  private static List<Range> getCustomRanges(final Category category)
  {
    final List<Range> result = new ArrayList<Range>();
    for (String uuid:settings.getList(category.getId() + ".custom",new String[0]))
    {
      result.add(new CustomRange(uuid));
    }
    return result;
  }
  
  /**
   * Löscht einen benutzerspezifischen Zeitraum.
   * @param category die Kategorie.
   * @param range der Zeitraum.
   */
  public static void deleteCustomRange(final Category category, CustomRange range)
  {
    final List<String> result = new ArrayList<>();
    for (String uuid:settings.getList(category.getId() + ".custom",new String[0]))
    {
      if (Objects.equals(uuid,range.uuid))
      {
        range.delete();
        continue;
      }
      result.add(uuid);
    }
    settings.setAttribute(category.getId() + "." + range.getId(), (String) null);
    settings.setAttribute(category.getId() + ".custom",result.toArray(new String[result.size()]));
  }

  /**
   * Speichert einen benutzerspezifischen Zeitraum.
   * @param category die Kategorie.
   * @param range der Zeitraum.
   */
  public static void saveCustomRange(final Category category, CustomRange range)
  {
    final List<String> result = new ArrayList<>();
    result.addAll(Arrays.asList(settings.getList(category.getId() + ".custom",new String[0])));
    if (!result.contains(range.uuid))
      result.add(range.uuid);

    range.save();
    settings.setAttribute(category.getId() + ".custom",result.toArray(new String[result.size()]));
  }

  /**
   * Speichert die fuer die Kategorie zu verwendenden Zeitraeume.
   * @param category Kategorie.
   * @param ranges Liste der anzuzeigenden Zeiträume für die gegebene Kategorie. 
   */
  public static void setActiveRanges(final Category category, List<Range> ranges)
  {
    for (Range range : getAllRanges(category))
    {
      settings.setAttribute(category.getId() + "." + range.getId(), ranges.contains(range));
    }
  }
  
  /**
   * Setzte die aktiven Zeitraeume auf die System-Vorgabe zurueck.
   * @param category Kategorie.
   */
  public static void resetActiveRanges(final Category category)
  {
    for (Range range : getAllRanges(category))
    {
      settings.setAttribute(category.getId() + "." + range.getId(), DEFAULT.contains(range));
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
      return null;
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
      return null;
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
      return null;
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
      return null;
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
  
  /**
   * Ein benutzerdefinierter Zeitraum.
   */
  public static class CustomRange extends Range
  {
    private String uuid = null;
    private int daysPast = 30;
    private int daysFuture = 30;
    private String name = i18n.tr("Benutzerdefinierter Zeitraum");
    
    /**
     * Erzeugt einen neuen benutzerspezfischen Zeitraum.
     * @return der neue benutzerspezifische Zeitraum.
     */
    public static CustomRange create()
    {
      return new CustomRange(UUID.randomUUID().toString());
    }
    
    /**
     * ct.
     * @param uuid die UUID des Zeitraumes.
     */
    private CustomRange(String uuid)
    {
      this.uuid = uuid;
      this.daysPast = settings.getInt(this.uuid + ".days.past",this.daysPast);
      this.daysFuture = settings.getInt(this.uuid + ".days.future",this.daysFuture);
      this.name = settings.getString(this.uuid + ".name",this.name);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getId()
     */
    @Override
    public String getId()
    {
      return this.uuid;
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getStart()
     */
    @Override
    public Date getStart()
    {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE,-this.getDaysPast());
      return DateUtil.startOfDay(cal.getTime());
    }
    
    /**
     * Liefert die Anzahl der Tage in der Vergangenheit.
     * @return die Anzahl der Tage in der Vergangenheit.
     */
    public int getDaysPast()
    {
      return this.daysPast;
    }
    
    /**
     * @see de.willuhn.jameica.hbci.server.Range#getEnd()
     */
    @Override
    public Date getEnd()
    {
      final Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE,this.getDaysFuture());
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * Liefert die Anzahl der Tage in der Zukunft.
     * @return die Anzahl der Tage in der Zukunft.
     */
    public int getDaysFuture()
    {
      return this.daysFuture;
    }
    
    /**
     * Speichert die Anzahl der Tage in der Vergangenheit.
     * @param days die Anzahl der Tage.
     */
    public void setDaysPast(int days)
    {
      this.daysPast = days;
    }
    
    /**
     * Speichert die Anzahl der Tage in der Zukunft.
     * @param days die Anzahl der Tage.
     */
    public void setDaysFuture(int days)
    {
      this.daysFuture = days;
    }
    
    /**
     * Speichert den Namen des Zeitraumes.
     * @param name der Name des Zeitraumes.
     */
    public void setName(String name)
    {
      this.name = name;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      return this.name;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
      if (!(obj instanceof CustomRange))
        return false;
      
      return Objects.equals(this.uuid,((CustomRange)obj).uuid);
    }
    
    /**
     * Löscht die Einstellungen des benutzerspezifischen Zeitraumes.
     */
    private void delete()
    {
      Logger.info("deleting custom range [name: " + this.name + "]");
      settings.setAttribute(this.uuid + ".days.past",(String)null);
      settings.setAttribute(this.uuid + ".days.future",(String)null);
      settings.setAttribute(this.uuid + ".days.name",(String)null);
    }
    
    /**
     * Speichert die Einstellungen des Zeitraumes.
     */
    private void save()
    {
      Logger.info("saving custom range [name: " + this.name + "]");
      settings.setAttribute(this.uuid + ".days.past",this.daysPast);
      settings.setAttribute(this.uuid + ".days.future",this.daysFuture);
      settings.setAttribute(this.uuid + ".name",this.name);
    }
  }
}
