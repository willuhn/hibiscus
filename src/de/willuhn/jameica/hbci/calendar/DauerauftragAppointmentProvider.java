/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/DauerauftragAppointmentProvider.java,v $
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
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Termin-Providers fuer anstehende Dauerauftraege.
 */
public class DauerauftragAppointmentProvider implements AppointmentProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private RGB color = null;
  
  /**
   * ct.
   */
  public DauerauftragAppointmentProvider()
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
      DBIterator list = service.createList(Dauerauftrag.class);

      List<Appointment> result = new LinkedList<Appointment>();
      while (list.hasNext())
      {
        // Wir checken, ob einer der Dauerauftraege am genannten Tag
        // ausgefuehrt wird oder wurde
        Dauerauftrag t = (Dauerauftrag) list.next();
        Date termin = getTermin(t,from,to);
        if (termin == null)
          continue; // Keine Zahlung in dem Zeitraum
        
        result.add(new MyAppointment(t,termin));
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
   * Prueft, ob der Dauerauftrag im genannten Zeitraum ausgefuehrt wird oder wurde.
   * @param t der Dauerauftrag.
   * @param from Start-Datum.
   * @param to End-Datum.
   * @return das Datum der Ausfuehrung oder NULL, wenn es im Zeitraum nicht ausgefuehrt wird.
   * @throws RemoteException
   */
  private Date getTermin(Dauerauftrag t, Date from, Date to) throws RemoteException
  {
    Date de       = t.getErsteZahlung();
    Date dl       = t.getLetzteZahlung();
    // Turnus turnus = t.getTurnus();
    
    // Auftrag faengt erst spaeter an
    if (de != null && de.after(to))
      return null;
    
    // Auftrag ist schon abgelaufen
    if (dl != null && dl.before(from))
      return null;

    
    // TODO Hier noch pruefen, ob der Turnus im angegebenen Zeitraum eine Zahlung vorsieht 
    
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Daueraufträge");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  public class MyAppointment implements Appointment
  {
    private Dauerauftrag t = null;
    private Date termin    = null;
    
    /**
     * ct.
     * @param t der Dauerauftrag.
     * @param termin der Termin der Ausfuehrung.
     */
    private MyAppointment(Dauerauftrag t, Date termin)
    {
      this.t = t;
      this.termin = termin;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#execute()
     */
    public void execute() throws ApplicationException
    {
      new DauerauftragNew().handleAction(this.t);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
     */
    public Date getDate()
    {
      return this.termin;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} an {2} als Dauerauftrag\n{3}\n\n{4}\n\nKonto: {5})",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),TurnusHelper.createBezeichnung(t.getTurnus()),VerwendungszweckUtil.toString(t),k.getLongName());
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
        return i18n.tr("Dauerauftrag");
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
 * $Log: DauerauftragAppointmentProvider.java,v $
 * Revision 1.1  2010/11/19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/