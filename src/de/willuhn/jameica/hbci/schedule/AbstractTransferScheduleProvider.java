/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/AbstractTransferScheduleProvider.java,v $
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
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung eines Schedule-Providers fuer Ueberweisungen und Lastschriften.
 * @param <T> der konkrete Auftragstyp.
 */
@Lifecycle(Type.REQUEST)
public abstract class AbstractTransferScheduleProvider<T extends Terminable & HibiscusDBObject> implements ScheduleProvider<T>
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private DBIterator list = null;
  
  /**
   * @see de.willuhn.jameica.hbci.schedule.ScheduleProvider#getSchedules(de.willuhn.jameica.hbci.rmi.Konto, java.util.Date, java.util.Date)
   */
  public List<Schedule<T>> getSchedules(Konto k, Date from, Date to)
  {
    List<Schedule<T>> result = new LinkedList<Schedule<T>>();

    Class type = BeanUtil.getType(this.getClass());
    if (type == null)
    {
      Logger.warn("schedule provider has no concrete type - unable to fetch data");
      return result;
    }
    
    try
    {
      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
      ReminderStorageProvider provider = bs.get(ReminderStorageProviderHibiscus.class);

      Date start = DateUtil.startOfDay(from);
      Date end   = DateUtil.endOfDay(to);
      Date now   = new Date();
      
      // wir cachen das Resultset von der Datenbank - dann muessen wir die
      // Daten nicht dauernd neu laden, wenn der User durch die Monate blaettert
      // Da wir einen Request-Scope haben, bleibt der Resultset auch nur solange
      // erhalten, wie sich der User auf der gleichen View befindet.
      if (list == null)
      {
        HBCIDBService service = Settings.getDBService();
        this.list = service.createList(type);
        if (k != null)
          list.addFilter("konto_id = " + k.getID());
        this.list.setOrder("ORDER BY " + service.getSQLTimestamp("termin"));
      }
      list.begin(); // rest pointer
      
      while (this.list.hasNext())
      {
        T u = (T) this.list.next();
        String uuid = u.getMeta("reminder.uuid",null);
        Date termin = u.getTermin();
        
        // a) Auftrag existiert. Wenn er ins Zeitfenster passt, wird er verwendet
        if (!termin.before(start) && !termin.after(end))
          result.add(new Schedule(termin,u,false));

        // b) jetzt noch checken, ob er einen Reminder hat.
        
        if (termin.after(end))
          continue; // hier brauchen wir gar nicht erst suchen - wir sind ausserhalb des Zeitfensters
        
        if (uuid != null)
        {
          Reminder reminder = provider.get(uuid);
          ReminderInterval ri = reminder != null ? reminder.getReminderInterval() : null;
          if (ri != null)
          {
            Date last = reminder.getEnd();
            List<Date> dates = ri.getDates(termin,new Date(termin.getTime()+1),end); // nicht ab start sondern ab (exclusive) erster Ausfuehrung
            
            // Wenn wir Termine haben, fuegen wir sie hinzu
            for (Date date:dates)
            {
              if (last != null && !last.after(end)) // bereits abgelaufen
                continue;
              
              // wir zeigen nur die kuenftigen an. Die vergangenen im
              // im aktuellen Zeitraum wurden ja schon automatisch erstellt
              // und wurden daher schon von a) erfasst
              if (date.after(now))
                result.add(new Schedule(date,u,true));
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return result;
  }
}



/**********************************************************************
 * $Log: AbstractTransferScheduleProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/