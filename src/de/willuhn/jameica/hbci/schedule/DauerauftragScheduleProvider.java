/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/DauerauftragScheduleProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.DauerauftragUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Schedule-Providers fuer anstehende Dauerauftraege.
 */
@Lifecycle(Type.REQUEST)
public class DauerauftragScheduleProvider implements ScheduleProvider<Dauerauftrag>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getSchedules(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  public List<Schedule<Dauerauftrag>> getSchedules(Konto k, Date from, Date to)
  {
    List<Schedule<Dauerauftrag>> result = new LinkedList<Schedule<Dauerauftrag>>();
    
    try
    {
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(Dauerauftrag.class);
      if (k != null)
        list.addFilter("konto_id = " + k.getID());

      while (list.hasNext())
      {
        // Wir checken, ob einer der Dauerauftraege am genannten Tag
        // ausgefuehrt wird oder wurde
        Dauerauftrag t = (Dauerauftrag) list.next();
        List<Date> termine = DauerauftragUtil.getTermine(t,from,to);
        if (termine == null || termine.size() == 0)
          continue; // Keine Zahlung in dem Zeitraum

        for (Date termin:termine)
          result.add(new Schedule(termin,t,false));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return result;
  }

  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Daueraufträge");
  }
}



/**********************************************************************
 * $Log: DauerauftragScheduleProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/