/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/AbstractTransferAppointmentProvider.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/12/13 23:15:11 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.lang.reflect.ParameterizedType;
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
import de.willuhn.jameica.hbci.reminder.ReminderStorageProviderHibiscus;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
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
 * Abstrakte Basis-Implementierung eines Termin-Providers fuer Ueberweisungen und Lastschriften.
 * @param <T> der konkrete Auftragstyp.
 */
public abstract class AbstractTransferAppointmentProvider<T extends Terminable & HibiscusDBObject> implements AppointmentProvider
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Class<T> type   = null;
  private DBIterator list = null;
  
  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getAppointments(java.util.Date, java.util.Date)
   */
  public List<Appointment> getAppointments(Date from, Date to)
  {
    try
    {
      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
      ReminderStorageProvider provider = bs.get(ReminderStorageProviderHibiscus.class);

      Date start = DateUtil.startOfDay(from);
      Date end   = DateUtil.endOfDay(to);
      Date now   = new Date();
      
      // wir cachen das Resultset von der Datenbank - dann muessen wir die
      // Daten nicht dauernd neu laden, wenn der User durch die Monate blaettert
      if (list == null)
      {
        HBCIDBService service = Settings.getDBService();
        this.list = service.createList(this.getType());
        this.list.setOrder("ORDER BY " + service.getSQLTimestamp("termin"));
      }
      list.begin(); // rest pointer
      
      List<Appointment> result = new LinkedList<Appointment>();
      while (this.list.hasNext())
      {
        T u = (T) this.list.next();
        String uuid = u.getMeta("reminder.uuid",null);
        Date termin = u.getTermin();
        
        // a) Auftrag existiert. Wenn er ins Zeitfenster passt, wird er angezeigt
        if (!termin.before(start) && !termin.after(end))
          result.add(this.createAppointment(u,null));

        // b) jetzt noch checken, ob er einen Reminder hat.
        
        if (termin.after(end))
          continue; // hier brauchen wir gar nicht erst suchen - wir sind ausserhalb des Zeitfensters
        
        if (uuid != null)
        {
          Reminder reminder = provider.get(uuid);
          ReminderInterval ri = reminder != null ? reminder.getReminderInterval() : null;
          if (ri != null)
          {
            List<Date> dates = ri.getDates(termin,new Date(termin.getTime()+1),end); // nicht ab start sondern ab (exclusive) erster Ausfuehrung
            
            // Wenn wir Termine haben, fuegen wir sie hinzu
            for (Date date:dates)
            {
              // wir zeigen nur die kuenftigen an. Die vergangenen im
              // im aktuellen Zeitraum wurden ja schon automatisch erstellt
              // und wurden daher schon von a) erfasst
              if (date.after(now))
                result.add(this.createAppointment(u,date));
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
   * Erzeugt das Appointment-Objekt.
   * @param t der Auftrag.
   * @param date der optionale Termin.
   * @return das Appointment-Objekt.
   */
  abstract AbstractTransferAppointment createAppointment(T t, Date date);
  
  /**
   * Liefert die konkrete Klasse des Typs.
   * @return die konkrete Klasse des Typs.
   */
  private Class<T> getType()
  {
    if (this.type == null)
    {
      // Gefunden in http://www.nautsch.net/2008/10/29/class-von-type-parameter-java-generics-gepimpt/
      // Generics-Voodoo ;)
      ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
      this.type = (Class<T>) type.getActualTypeArguments()[0];
    }
    return this.type;
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  abstract class AbstractTransferAppointment extends AbstractAppointment
  {
    protected T         t = null;
    protected Date date   = null;
    
    /**
     * ct.
     * @param t der Auftrag.
     * @param date ggf abweichender Termin.
     */
    protected AbstractTransferAppointment(T t, Date date)
    {
      this.t    = t;
      this.date = date;
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
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#hasAlarm()
     */
    public boolean hasAlarm()
    {
      try
      {
        return this.date != null || !this.t.ausgefuehrt(); // entweder Wiederholung oder noch nicht ausgefuehrt
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
 * $Log: AbstractTransferAppointmentProvider.java,v $
 * Revision 1.2  2011/12/13 23:15:11  willuhn
 * @B Wenn wir vor dem moeglichen Wiederholungszeitraum sind, brauchen wir nicht nach Remindern suchen
 *
 * Revision 1.1  2011/12/13 23:10:21  willuhn
 * @N BUGZILLA 1162
 *
 **********************************************************************/