/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.I18N;

/**
 * Bean mit moeglichen Zeitraeumen.
 */
public abstract class Range
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Bekannte Zeitraeume.
   */
  public final static List<Range> KNOWN = new ArrayList<Range>()
  {{
    add(new ThisWeek());
    add(new LastWeek());
    add(new ThisMonth());
    add(new LastMonth());
    add(new SecondLastMonth());
    add(new ThisQuarter());
    add(new LastQuarter());
    add(new ThisYear());
    add(new LastYear());
  }};
  
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
      return i18n.tr("Diese Woche");
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
      cal.setTime(new ThisWeek().getEnd());
      cal.add(Calendar.WEEK_OF_YEAR,-1);
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Letzte Woche");
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
      return i18n.tr("Dieser Monat");
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
      cal.add(Calendar.MONTH,-1);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Letzter Monat");
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
      cal.add(Calendar.MONTH,-2);
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      return DateUtil.endOfDay(cal.getTime());
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return i18n.tr("Vorletzter Monat");
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
      return i18n.tr("Dieses Quartal");
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
      return i18n.tr("Letztes Quartal");
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
      return i18n.tr("Dieses Jahr");
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
      return i18n.tr("Letztes Jahr");
    }
  }

}


