/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/AuslandsUeberweisungAppointmentProvider.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/11/22 00:52:53 $
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
import de.willuhn.jameica.gui.calendar.Appointment;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Termin-Providers fuer offene SEPA-Ueberweisungen.
 */
public class AuslandsUeberweisungAppointmentProvider implements AppointmentProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getAppointments(java.util.Date, java.util.Date)
   */
  public List<Appointment> getAppointments(Date from, Date to)
  {
    try
    {
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(AuslandsUeberweisung.class);
      if (from != null) list.addFilter("termin >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(from).getTime())});
      if (to   != null) list.addFilter("termin <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(to).getTime())});
      list.setOrder("ORDER BY " + service.getSQLTimestamp("termin"));

      List<Appointment> result = new LinkedList<Appointment>();
      while (list.hasNext())
        result.add(new MyAppointment((AuslandsUeberweisung) list.next()));
      
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
    return i18n.tr("Offene SEPA-Überweisungen");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment implements Appointment
  {
    private AuslandsUeberweisung t = null;
    
    /**
     * ct.
     * @param t die SEPA-Ueberweisung.
     */
    private MyAppointment(AuslandsUeberweisung t)
    {
      this.t = t;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
     */
    public void execute() throws ApplicationException
    {
      new AuslandsUeberweisungNew().handleAction(this.t);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
     */
    public Date getDate()
    {
      try
      {
        return t.getTermin();
      }
      catch (Exception e)
      {
        Logger.error("unable to read date",e);
      }
      return null;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} an {2} überweisen\n\n{3}\n\nKonto: {4}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),VerwendungszweckUtil.toString(t),k.getLongName());
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
        return i18n.tr("SEPA-Überweisung");
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
     */
    public RGB getColor()
    {
      try
      {
        if (t.ausgefuehrt())
          return Color.COMMENT.getSWTColor().getRGB();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine execution status",re);
      }
      return Settings.getBuchungSollForeground().getRGB();
    }
  }
}



/**********************************************************************
 * $Log: AuslandsUeberweisungAppointmentProvider.java,v $
 * Revision 1.3  2010/11/22 00:52:53  willuhn
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