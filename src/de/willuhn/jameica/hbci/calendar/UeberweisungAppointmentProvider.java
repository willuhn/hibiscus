/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/UeberweisungAppointmentProvider.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/12/10 00:25:48 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.calendar.AbstractAppointment;
import de.willuhn.jameica.gui.calendar.Appointment;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
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
 * Implementierung eines Termin-Providers fuer offene Ueberweisungen.
 */
public class UeberweisungAppointmentProvider implements AppointmentProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getAppointments(java.util.Date, java.util.Date)
   */
  public List<Appointment> getAppointments(Date from, Date to)
  {
    try
    {
      Date start = DateUtil.startOfDay(from);
      Date end   = DateUtil.endOfDay(to);
      Date now   = new Date();
      
      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
      ReminderStorageProvider provider = bs.get(ReminderStorageProviderHibiscus.class);

      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(Ueberweisung.class);
      list.setOrder("ORDER BY " + service.getSQLTimestamp("termin"));

      List<Appointment> result = new LinkedList<Appointment>();
      while (list.hasNext())
      {
        Ueberweisung u = (Ueberweisung) list.next();
        String uuid = u.getMeta("reminder.uuid",null);
        Date termin = u.getTermin();
        
        // a) Auftrag existiert. Wenn er ins Zeitfenster passt, wird er angezeigt
        if (!termin.before(start) && !termin.after(end))
          result.add(new MyAppointment(u,null));

        // b) jetzt noch checken, ob er einen Reminder hat.
        if (uuid != null)
        {
          Reminder reminder = provider.get(uuid);
          ReminderInterval ri = reminder != null ? reminder.getReminderInterval() : null;
          if (ri != null)
          {
            List<Date> dates = ri.getDates(termin,start,end);
            
            // Wenn wir Termine haben, fuegen wir sie hinzu
            for (Date date:dates)
            {
              // wir zeigen nur die kuenftigen an. Die vergangenen im
              // im aktuellen Zeitraum wurden ja schon automatisch erstellt
              // und wurden daher schon von a) erfasst
              if (date.after(now))
                result.add(new MyAppointment(u,date));
            }
          }
        }
      }
      
      return result;
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Überweisungen");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment extends AbstractAppointment
  {
    private Ueberweisung t = null;
    private Date date      = null;
    
    /**
     * ct.
     * @param t die Ueberweisung.
     * @param date ggf abweichender Termin.
     */
    private MyAppointment(Ueberweisung t, Date date)
    {
      this.t = t;
      this.date = date;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#execute()
     */
    public void execute() throws ApplicationException
    {
      new UeberweisungNew().handleAction(this.t);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
     */
    public Date getDate()
    {
      try
      {
        return this.date != null ? this.date : this.t.getTermin();
      }
      catch (Exception e)
      {
        Logger.error("unable to read date",e);
      }
      return null;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
        Konto k = t.getKonto();
        return i18n.tr("{0}Überweisung: {1} {2} an {3} überweisen\n\n{4}\n\nKonto: {5}",(this.date != null ? (i18n.tr("Geplant") + ":\n") : ""),HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),VerwendungszweckUtil.toString(t,"\n"),k.getLongName());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build description",re);
        return null;
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getName()
     */
    public String getName()
    {
      try
      {
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} an {2}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("Überweisung");
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getColor()
     */
    public RGB getColor()
    {
      // Grau anzeigen, wenn er schon ausgefuehrt wurde oder
      // es eine noch nicht existierende Wiederholung ist.
      if (!this.hasAlarm() || this.date != null)
        return Color.COMMENT.getSWTColor().getRGB();
      
      return Settings.getBuchungSollForeground().getRGB();
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getUid()
     */
    public String getUid()
    {
      try
      {
        return "hibiscus.ueb." + t.getID();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create uid",re);
        return super.getUid();
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#hasAlarm()
     */
    public boolean hasAlarm()
    {
      try
      {
        return this.date != null || !this.t.ausgefuehrt(); //entweder Wiederholung oder noch nicht ausgefuehrt
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine execution status",re);
        return super.hasAlarm();
      }
    }
  }
}



/**********************************************************************
 * $Log: UeberweisungAppointmentProvider.java,v $
 * Revision 1.7  2011/12/10 00:25:48  willuhn
 * @N BUGZILLA 1162 - Anzeige der geplanten Wiederholungen im Kalender
 *
 * Revision 1.6  2011/10/21 10:53:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2011/10/06 10:49:24  willuhn
 * @N Termin-Provider fuer Umsaetze
 *
 * Revision 1.4  2011-01-20 17:12:39  willuhn
 * @C geaendertes Appointment-Interface
 *
 * Revision 1.3  2010-11-22 00:52:53  willuhn
 * @C Appointment-Inner-Class darf auch private sein
 *
 * Revision 1.2  2010-11-21 23:31:26  willuhn
 * @N Auch abgelaufene Termine anzeigen
 * @N Turnus von Dauerauftraegen berechnen
 *
 * Revision 1.1  2010-11-19 18:37:20  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/