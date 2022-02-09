/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.calendar.AbstractAppointment;
import de.willuhn.jameica.gui.calendar.Appointment;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.schedule.ScheduleProvider;
import de.willuhn.jameica.hbci.schedule.ScheduleProviderFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Implementierung eines Termin-Providers.
 * @param <T> der konkrete Typ des Termin-Providers.
 */
public abstract class AbstractAppointmentProvider<T extends HibiscusDBObject> implements AppointmentProvider
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private ScheduleProvider provider = null;
  
  @Override
  public List<Appointment> getAppointments(Date from, Date to)
  {
    List<Appointment> result = new LinkedList<Appointment>();

    ScheduleProvider provider = this.getScheduleProvider();
    if (provider == null)
    {
      Logger.warn("unable to determine schedule provider for " + this.getClass().getSimpleName());
      return result;
    }
    
    List<Schedule<T>> list = provider.getSchedules(null,from,to);

    // In Appointments kopieren
    for (Schedule<T> schedule:list)
    {
      result.add(this.createAppointment(schedule));
    }
    return result;
  }
  
  /**
   * Liefert den passenden Schedule-Provider.
   * @return der Schedule-Provider.
   */
  private ScheduleProvider getScheduleProvider()
  {
    if (this.provider == null)
      this.provider = ScheduleProviderFactory.getScheduleProvider(BeanUtil.getType(this.getClass()));
    return this.provider;
  }

  /**
   * Erzeugt das Appointment-Objekt.
   * @param schedule das Schedule.
   * @return das Appointment-Objekt.
   */
  abstract AbstractHibiscusAppointment createAppointment(Schedule<T> schedule);

  @Override
  public String getName()
  {
    ScheduleProvider provider = this.getScheduleProvider();
    return provider != null ? provider.getName() : "<unknown provider>";
  }

  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  abstract class AbstractHibiscusAppointment extends AbstractAppointment
  {
    protected Schedule<T> schedule = null;
    
    @Override
    public void execute() throws ApplicationException
    {
      new Open().handleAction(this.schedule.getContext());
    }

    /**
     * ct.
     * @param schedule der Auftrag.
     */
    protected AbstractHibiscusAppointment(Schedule<T> schedule)
    {
      this.schedule = schedule;
    }

    @Override
    public Date getDate()
    {
      return this.schedule.getDate();
    }

    @Override
    public RGB getColor()
    {
      // Grau anzeigen, wenn kein Alarm mehr noetig ist oder er nur geplant ist,
      // aber noch nicht existiert
      if (this.schedule.isPlanned() || !this.hasAlarm())
        return Color.COMMENT.getSWTColor().getRGB();
      
      return Settings.getBuchungSollForeground().getRGB();
    }

    @Override
    public boolean hasAlarm()
    {
      // Ist geplant - also benachrichtigen
      if (this.schedule.isPlanned())
        return true;
          
      try
      {
        T t = this.schedule.getContext();
        if (t instanceof Terminable)
          return !((Terminable)t).ausgefuehrt(); // noch nicht ausgefuehrt
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine execution status",re);
      }
      return super.hasAlarm();
    }
    
    @Override
    public String getUid()
    {
      try
      {
        T t = this.schedule.getContext();
        return t.getClass().getName() + "." + t.getID();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create uid",re);
        return super.getUid();
      }
    }
  }
}



/**********************************************************************
 * $Log: AbstractAppointmentProvider.java,v $
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/