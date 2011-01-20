/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/DauerauftragAppointmentProvider.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/01/20 17:12:39 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
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
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Turnus;
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
        List<Date> termine = getTermine(t,from,to);
        if (termine == null || termine.size() == 0)
          continue; // Keine Zahlung in dem Zeitraum

        for (Date termin:termine)
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
   * @return die Termine, zu denen der Auftrag im angegebenen Zeitraum ausgefuehrt wird.
   * @throws RemoteException
   */
  private List<Date> getTermine(Dauerauftrag t, Date from, Date to) throws RemoteException
  {
    Date de       = t.getErsteZahlung();
    Date dl       = t.getLetzteZahlung();
    Turnus turnus = t.getTurnus();
    
    List<Date> result = new ArrayList<Date>();
    
    // Auftrag faengt erst spaeter an
    if (de != null && de.after(to))
      return result;
    
    // Auftrag ist schon abgelaufen
    if (dl != null && dl.before(from))
      return result;
    
    // Wir machen maximal 100 Iterationen. Das hab ich jetzt willkuerlich
    // festgelegt. Wenn das Zeitfenster 1 Monat ist, koennen es ohnehin nur
    // maximal 5 Termine sein. Fuer den Fall, dass das Zeitfenster aber mal
    // groesser ist, machen wir ein paar mehr Iterationen
    Date start = from;
    if (de.after(start)) // Der Auftrag faengt erst mitten im Zeitraum an
      start = de;
    
    for (int i=0;i<100;++i)
    {
      if (start.after(to))
        break; // Wir sind raus
      
      // Als Valuta nehmen wir den ersten des Monats
      Date d = TurnusHelper.getNaechsteZahlung(de,dl,turnus,start);
      
      // Wir haben keine weiteren Termine mehr gefunden oder sind aus dem Zeitfenster raus
      if (d == null || d.after(to))
        break;

      // Tag uebernehmen
      result.add(d);

      // Noch einen Tag weiterruecken
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      cal.add(Calendar.DAY_OF_MONTH,1);
      d = cal.getTime();
        
      // Und wir machen beim naechsten Tag weiter
      start = d;
      
    }
    return result;
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
  private class MyAppointment extends AbstractAppointment
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
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#execute()
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
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
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
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getColor()
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

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getUid()
     */
    public String getUid()
    {
      try
      {
        return "hibiscus.dauer." + t.getID();
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
 * $Log: DauerauftragAppointmentProvider.java,v $
 * Revision 1.6  2011/01/20 17:12:39  willuhn
 * @C geaendertes Appointment-Interface
 *
 * Revision 1.5  2010-11-23 11:47:35  willuhn
 * @B Mehrfachzahlungen innerhalb eines Monats wurden nicht beruecksichtigt
 *
 * Revision 1.4  2010-11-22 00:52:53  willuhn
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