/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Enum mit den unterstuetzten Intervalls beim Abruf der PDF-Kontoauszuege.
 */
public abstract class KontoauszugInterval
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Das Default-Intervall.
   */
  public static KontoauszugInterval DEFAULT = new IntervalNever();
  
  /**
   * Die Liste der bekannten Intervalle.
   */
  public final static List<KontoauszugInterval> KNOWN = Arrays.asList(
      new IntervalNever(),
      new IntervalAlways(),
      new IntervalWeekly(),
      new IntervalBiWeekly(),
      new IntervalMonthly()
  );

  /**
   * Ermittelt das naechste Abrufdatum fuer die Kontoauszuege im PDF-Format.
   * @param k das zu pruefende Konto.
   * @return das Datum fuer den naechsten Abruf oder NULL, wenn keines ermittelbar ist.
   * @throws RemoteException
   */
  public static Date getNextInterval(Konto k) throws RemoteException
  {
    // Checken, was als Intervall eingestellt ist.
    KontoauszugInterval type = find(MetaKey.KONTOAUSZUG_INTERVAL.get(k));
    if (type == null)
      return null;
    
    // Datum des letzten Abrufs ermitteln
    Date last = null;
    String s = null;
    try
    {
      s = MetaKey.KONTOAUSZUG_INTERVAL_LAST.get(k);
      if (s != null && s.length() > 0)
        last = HBCI.LONGDATEFORMAT.parse(s);
    }
    catch (Exception e)
    {
      Logger.error("unparsable date: " + s,e);
    }
    
    return type.getNextInterval(last);
  }
  
  /**
   * Liefert das Intervall basierend auf der ID.
   * @param id die ID des Intervall.
   * @return das Intervall oder NULL, wenn es nicht gefunden wurde.
   */
  public static KontoauszugInterval find(String id)
  {
    if (id == null || id.length() == 0)
      return null;
    
    for (KontoauszugInterval k:KNOWN)
    {
      if (k.getId().equals(id))
        return k;
    }
    
    return null;
  }
  
  /**
   * Ermittelt das naechste Intervall basierend auf dem letzten Termin.
   * @param last der letzte Termin. Kann NULL sein.
   * @return das naechste Datum oder NULL, wenn keines existiert.
   */
  public abstract Date getNextInterval(Date last);
  
  /**
   * Liefert einen Identifier fuer das Intervall.
   * @return ein Identifier fuer das Intervall.
   */
  public abstract String getId();
  
  /**
   * Liefert einen sprechenden Namen fuer das Intervall.
   * @return sprechender Name fuer das Intervall.
   */
  public abstract String getName();
  
  /**
   * Intervall fuer niemals.
   */
  public static class IntervalNever extends KontoauszugInterval
  {
    @Override
    public Date getNextInterval(Date last)
    {
      return null;
    }

    @Override
    public String getId()
    {
      return "never";
    }

    @Override
    public String getName()
    {
      return i18n.tr("Nie (nur manuell)");
    }
  }

  /**
   * Intervall fuer immer.
   */
  public static class IntervalAlways extends KontoauszugInterval
  {
    @Override
    public Date getNextInterval(Date last)
    {
      return DateUtil.startOfDay(new Date());
    }

    @Override
    public String getId()
    {
      return "always";
    }

    @Override
    public String getName()
    {
      return i18n.tr("immer");
    }
  }
  
  /**
   * Intervall fuer woechentlich.
   */
  public static class IntervalWeekly extends KontoauszugInterval
  {

    @Override
    public Date getNextInterval(Date last)
    {
      if (last == null)
        return DateUtil.startOfDay(new Date());
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(last);
      cal.add(Calendar.DATE,7);
      return DateUtil.startOfDay(cal.getTime());
    }

    @Override
    public String getId()
    {
      return "weekly";
    }

    @Override
    public String getName()
    {
      return i18n.tr("Wöchentlich");
    }
  }

  
  /**
   * Intervall fuer zwei-woechentlich.
   */
  public static class IntervalBiWeekly extends KontoauszugInterval
  {

    @Override
    public Date getNextInterval(Date last)
    {
      if (last == null)
        return DateUtil.startOfDay(new Date());
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(last);
      cal.add(Calendar.DATE,14);
      return DateUtil.startOfDay(cal.getTime());
    }

    @Override
    public String getId()
    {
      return "biweekly";
    }

    @Override
    public String getName()
    {
      return i18n.tr("Alle 2 Wochen");
    }
  }

  
  public static class IntervalMonthly extends KontoauszugInterval
  {

    @Override
    public Date getNextInterval(Date last)
    {
      if (last == null)
        return DateUtil.startOfDay(new Date());
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(last);
      cal.add(Calendar.MONTH,1);
      return DateUtil.startOfDay(cal.getTime());
    }

    @Override
    public String getId()
    {
      return "monthly";
    }

    @Override
    public String getName()
    {
      return i18n.tr("Monatlich");
    }
  }
}


