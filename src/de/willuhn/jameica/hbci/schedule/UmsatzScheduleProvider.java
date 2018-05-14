/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Schedule-Providers fuer die Umsaetze.
 */
@Lifecycle(Type.REQUEST)
public class UmsatzScheduleProvider implements ScheduleProvider<Umsatz>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getSchedules(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  public List<Schedule<Umsatz>> getSchedules(Konto k, Date from, Date to)
  {
    List<Schedule<Umsatz>> result = new LinkedList<Schedule<Umsatz>>();
    
    try
    {
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(Umsatz.class);
      if (k != null)
        list.addFilter("konto_id = " + k.getID());

      if (from != null) list.addFilter("datum >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(from).getTime())});
      if (to   != null) list.addFilter("datum <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(to).getTime())});
      list.setOrder("ORDER BY " + service.getSQLTimestamp("datum"));

      while (list.hasNext())
      {
        Umsatz u = (Umsatz) list.next();
        result.add(new Schedule(u.getDatum(),u,false));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return result;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Umsätze");
  }
}



/**********************************************************************
 * $Log: UmsatzScheduleProvider.java,v $
 * Revision 1.2  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/