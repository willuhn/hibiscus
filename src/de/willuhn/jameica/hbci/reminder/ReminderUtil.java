/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.reminder;

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.reminder.Reminder;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.reminder.ReminderStorageProvider;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
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
    {
      Logger.info("type " + order.getClass().getName() + " does not support reminders");
      throw new ApplicationException(i18n.tr("Der Auftrag unterstützt keine Termine"));
    }

    try
    {
      order.transactionBegin();

      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      ReminderStorageProvider provider = service.get(ReminderStorageProviderHibiscus.class);

      MetaKey UUID = MetaKey.REMINDER_UUID;

      // Reminder laden
      String uuid       = UUID.get(order);
      Reminder reminder = (uuid != null ? provider.get(uuid) : null);

      // a) ohne Intervall
      if (interval == null)
      {
        // wenn wir schon einen Reminder haben, dann loeschen wir ihn
        if (reminder != null)
        {
          provider.delete(uuid);
          UUID.set(order,null); // Referenz loeschen
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
        reminder.setEnd(DateUtil.endOfDay(end)); // BUGZILLA 1585 - Datum auf Ende des Tages setzen, um sicherzustellen, dass der Termin im Laufe des Tages noch gilt
        reminder.setData(Reminder.KEY_EXPIRED,null); // expired date resetten. Wird vom Service bei Bedarf neu gesetzt
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
          UUID.set(order,uuid);
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