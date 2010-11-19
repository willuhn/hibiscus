/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/SammelUeberweisungAppointmentProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/19 18:37:19 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Termin-Providers fuer offene Sammel-Ueberweisungen.
 */
public class SammelUeberweisungAppointmentProvider implements AppointmentProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private RGB color = null;
  
  /**
   * ct.
   */
  public SammelUeberweisungAppointmentProvider()
  {
    this.color = Settings.getBuchungSollForeground().getRGB();
  }
  
  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getAppointments(java.util.Date, java.util.Date)
   */
  public List<Appointment> getAppointments(Date from, Date to)
  {
    try
    {
      HBCIDBService service = Settings.getDBService();
      DBIterator list = service.createList(SammelUeberweisung.class);
      list.addFilter("ausgefuehrt = 0");
      if (from != null) list.addFilter("termin >= ?", new Object[]{new java.sql.Date(HBCIProperties.startOfDay(from).getTime())});
      if (to   != null) list.addFilter("termin <= ?", new Object[]{new java.sql.Date(HBCIProperties.endOfDay(to).getTime())});
      list.setOrder("ORDER BY " + service.getSQLTimestamp("termin"));

      List<Appointment> result = new LinkedList<Appointment>();
      while (list.hasNext())
        result.add(new MyAppointment((SammelUeberweisung) list.next()));
      
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
    return i18n.tr("Offene Sammelüberweisungen");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  public class MyAppointment implements Appointment
  {
    private SammelUeberweisung t = null;
    
    /**
     * ct.
     * @param t die Sammel-Ueberweisung.
     */
    private MyAppointment(SammelUeberweisung t)
    {
      this.t = t;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
     */
    public void execute() throws ApplicationException
    {
      new SammelUeberweisungNew().handleAction(this.t);
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
        return i18n.tr("{0} {1} überweisen\n\n{2}\n\nKonto: {3})",HBCI.DECIMALFORMAT.format(t.getSumme()),k.getWaehrung(),t.getBezeichnung(),k.getLongName());
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
        return i18n.tr("{0} {1} {2}",HBCI.DECIMALFORMAT.format(t.getSumme()),k.getWaehrung(),t.getBezeichnung());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("Sammelüberweisung");
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getColor()
     */
    public RGB getColor()
    {
      return color;
    }
  }
}



/**********************************************************************
 * $Log: SammelUeberweisungAppointmentProvider.java,v $
 * Revision 1.1  2010/11/19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/