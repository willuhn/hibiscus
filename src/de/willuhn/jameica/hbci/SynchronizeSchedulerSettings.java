/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

/**
 * Hält die Einstellungen für die automatisierte Synchronisierung.
 */
public class SynchronizeSchedulerSettings
{
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(SynchronizeSchedulerSettings.class);
  
  /**
   * Liefert true, wenn die automatisierte Synchronisierung aktiv ist.
   * @return true, wenn die automatisierte Synchronisierung aktiv ist.
   */
  public static boolean isEnabled()
  {
    return settings.getBoolean("enabled",false);
  }
  
  /**
   * Legt fest, ob die automatisierte Synchronisierung aktiv ist.
   * @param b true, wenn die automatisierte Synchronisierung aktiv ist.
   */
  public static void setEnabled(boolean b)
  {
    settings.setAttribute("enabled",b);
  }
  
  /**
   * Liefert das Scheduler-Intervall in Minuten.
   * @return Scheduler-Intervall.
   */
  public static int getSchedulerInterval()
  {
    return settings.getInt("interval.minutes",180);
  }
  
  /**
   * Speichert das Scheduler-Intervall.
   * @param minutes
   */
  public static void setSchedulerInterval(int minutes)
  {
    settings.setAttribute("interval.minutes",Math.abs(minutes));
  }
  
  /**
   * Liefert die Uhrzeit, ab der der Scheduler beginnen soll.
   * Da Banken nachts ihre Buchungslaeufe durchfuehren, werden die HBCI-System
   * in dieser Zeit oft deaktiviert. Damit Hibiscus hierdurch nicht unnoetig Fehler
   * produziert, kann er in dieser Zeit pausiert werden.
   * @return Beginn-Uhrzeit des Schedulers (Angabe in Stunden).
   */
  public static int getSchedulerStartTime()
  {
    return settings.getInt("start.hour",06);
  }
  
  /**
   * Speichert die Uhrzeit, ab der der Scheduler beginnen soll.
   * @param hour Beginn-Uhrzeit des Schedulers (Angabe in Stunden).
   */
  public static void setSchedulerStartTime(int hour)
  {
    settings.setAttribute("start.hour",hour);
  }
  
  /**
   * Liefert die Uhrzeit, zu der der Scheduler enden soll.
   * Da Banken nachts ihre Buchungslaeufe durchfuehren, werden die HBCI-System
   * in dieser Zeit oft deaktiviert. Damit Hibiscus hierdurch nicht unnoetig Fehler
   * produziert, kann er in dieser Zeit pausiert werden.
   * @return End-Uhrzeit des Schedulers (Angabe in Stunden).
   */
  public static int getSchedulerEndTime()
  {
    return settings.getInt("end.hour",23);
  }
  
  /**
   * Speichert die Uhrzeit, zu der der Scheduler enden soll.
   * @param hour End-Uhrzeit des Schedulers (Angabe in Stunden).
   */
  public static void setSchedulerEndTime(int hour)
  {
    settings.setAttribute("end.hour",hour);
  }
  
  /**
   * Liefert true, wenn der Scheduler am genannten Tag laufen soll.
   * @param day der Tag - gemass {@link java.util.Calendar#MONDAY},{@link java.util.Calendar#TUESDAY},...
   * @return true, wenn der Scheduler am genannten Tag laufen soll.
   */
  public static boolean getSchedulerIncludeDay(int day)
  {
    return settings.getBoolean("include.day." + day,true);
  }

  /**
   * Legt fest, ob der Scheduler am genannten Tag laufen soll.
   * @param day der Tag - gemass {@link java.util.Calendar#MONDAY},{@link java.util.Calendar#TUESDAY},...
   * @param b true, wenn der Scheduler am genannten Tag laufen soll.
   */
  public static void setSchedulerIncludeDay(int day, boolean b)
  {
    settings.setAttribute("include.day." + day,b);
  }

  /**
   * Legt fest, ob der Scheduler im Fehlerfall beendet werden soll.
   * @return true, wenn der Scheduler im Fehlerfall angehalten wird.
   */
  public static boolean getStopSchedulerOnError()
  {
    return settings.getBoolean("stoponerror",true);
  }
  
  /**
   * Legt fest, ob der Scheduler im Fehlerfall beendet werden soll.
   * @param stop true, wenn der Scheduler im Fehlerfall angehalten wird.
   */
  public static void setStopSchedulerOnError(boolean stop)
  {
    settings.setAttribute("stoponerror",stop);
  }
}
