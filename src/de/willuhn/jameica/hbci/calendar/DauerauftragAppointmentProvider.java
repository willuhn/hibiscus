/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/DauerauftragAppointmentProvider.java,v $
 * $Revision: 1.4 $
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
    
    // Auftrag faengt erst spaeter an
    if (de != null && de.after(to))
      return null;
    
    // Auftrag ist schon abgelaufen
    if (dl != null && dl.before(from))
      return null;

    // Als Valuta nehmen wir den ersten des Monats
    Date d = TurnusHelper.getNaechsteZahlung(de,dl,t.getTurnus(),from);
    if (d == null)
      return null;
    // Jetzt muessen wir nur noch schauen, ob sich das Datum im aktuellen Monat befindet
    return (!d.after(to)) ? d : null;
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
  private class MyAppointment implements Appointment
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
        return i18n.tr("{0} {1} an {2}\n{3}\n\n{4}\n\nKonto: {5}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),TurnusHelper.createBezeichnung(t.getTurnus()),VerwendungszweckUtil.toString(t),k.getLongName());
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
      // Hier gibt es keinen Ausgefuehrt-Status.
      // Wir markieren ihn grau, wenn er in der Vergangenheit liegt,
      // ansonsten farbig
      if (this.termin != null && this.termin.before(new Date()))
        return Color.COMMENT.getSWTColor().getRGB();
      return Settings.getBuchungSollForeground().getRGB();
    }
  }
}



/**********************************************************************
 * $Log: DauerauftragAppointmentProvider.java,v $
 * Revision 1.4  2010/11/22 00:52:53  willuhn
 * @C Appointment-Inner-Class darf auch private sein
 *
 * Revision 1.3  2010-11-21 23:57:57  willuhn
 * @N Wir merken uns das letzte Datum und springen wieder zu dem zurueck, wenn wir z.Bsp. aus der Detail-Ansicht eines Auftrages zurueckkommen
 *
 * Revision 1.2  2010-11-21 23:31:26  willuhn
 * @N Auch abgelaufene Termine anzeigen
 * @N Turnus von Dauerauftraegen berechnen
 *
 * Revision 1.1  2010-11-19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/