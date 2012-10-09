/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/reminder/ReminderUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.reminder;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein paar statische Hilfsfunktionen.
 */
public class ReminderUtil
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Uebernimmt ein Reminder-Intervall in einen Auftrag oder entfernt es wieder (wenn "interval" null ist).
   * @param order der Auftrag.
   * @param interval das Intervall
   * @param end optionales Ende-Datum.
   * @throws Exception
   */
  public static void apply(HibiscusDBObject order, ReminderInterval interval, Date end) throws Exception
  {
    if (!(order instanceof Terminable))
      throw new ApplicationException(i18n.tr("Der Auftrag unterstützt keine Termine"));
    
    try
    {
      order.transactionBegin();

      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);

      // Reminder laden
      String uuid       = order.getMeta("reminder.uuid",null);
      Reminder reminder = (uuid != null ? provider.get(uuid) : null);

      // a) ohne Intervall
      if (interval == null)
      {
        // wenn wir schon einen Reminder haben, dann loeschen wir ihn
        if (reminder != null)
        {
          provider.delete(uuid);
          order.setMeta("reminder.uuid",null); // Referenz loeschen
        }
      }
      else // b) mit Intervall
      {
        // Neuen Reminder anlegen?
        if (reminder == null)
          reminder = new Reminder();

        // Wichtig: Das Start-Datum des Reminders ist nicht direkt der
        // Termin sondern {Termin + ersters Intervall}. Die erste
        // Ausfuehrung ist ja der Auftrag "bu" selbst. Erst die Folge-Termine
        // kommen ja via Reminder.
        // Das erste Datum ignorieren wir. Wir wollen ja erst das naechste haben.
        // Selbst bei jaehrlicher Ausfuehrung sollte die Liste jetzt genau
        // 2 Daten enthalten (erster und letzter des Jahres).
        List<Date> dates = interval.getDates(((Terminable)order).getTermin(),null,null);
        if (dates.size() < 2)
          throw new ApplicationException(i18n.tr("Kein Datum für die nächste Wiederholung ermittelbar"));
        reminder.setDate(dates.get(1));
        reminder.setEnd(end);
        reminder.setQueue("hibiscus.reminder.order");
        reminder.setData("order.class",order.getClass().getName());
        reminder.setData("order.id",order.getID());
        reminder.setReminderInterval(interval);
        
        if (uuid != null) // Update
        {
          provider.update(uuid,reminder);
        }
        else // Add
        {
          uuid = provider.add(reminder);
          // UUID des Reminders speichern
          order.setMeta("reminder.uuid",uuid);
        }
      }
      
      order.transactionCommit();
    }
    catch (Exception e)
    {
      try
      {
        order.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw e;
    }
  }
}



/**********************************************************************
 * $Log: ReminderUtil.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/