/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/DauerauftragAppointmentProvider.java,v $
 * $Revision: 1.10 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer anstehende Dauerauftraege.
 */
public class DauerauftragAppointmentProvider extends AbstractAppointmentProvider<Dauerauftrag>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  AbstractHibiscusAppointment createAppointment(Schedule<Dauerauftrag> schedule)
  {
    return new MyAppointment(schedule);
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment extends AbstractHibiscusAppointment
  {
    /**
     * ct.
     * @param schedule der Termin.
     */
    private MyAppointment(Schedule<Dauerauftrag> schedule)
    {
      super(schedule);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
        Dauerauftrag t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("Dauerauftrag: {0} {1} an {2}\n{3}\n\n{4}\n\nKonto: {5}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),TurnusHelper.createBezeichnung(t.getTurnus()),VerwendungszweckUtil.toString(t,"\n"),k.getLongName());
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
        Dauerauftrag t = this.schedule.getContext();
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
     * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider.AbstractHibiscusAppointment#getColor()
     */
    public RGB getColor()
    {
      // Hier gibt es keinen Ausgefuehrt-Status.
      // Wir markieren ihn grau, wenn er in der Vergangenheit liegt,
      // ansonsten farbig
      Date termin = this.schedule.getDate();
      
      if (termin != null && termin.before(new Date()))
        return Color.COMMENT.getSWTColor().getRGB();
      return Settings.getBuchungSollForeground().getRGB();
    }

    /**
     * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider.AbstractHibiscusAppointment#hasAlarm()
     */
    public boolean hasAlarm()
    {
      // brauchen wir bei Dauerauftraegen nicht - da kuemmert sich die Bank drum
      return false;
    }
  }
}



/**********************************************************************
 * $Log: DauerauftragAppointmentProvider.java,v $
 * Revision 1.10  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 * Revision 1.9  2012/02/05 12:03:43  willuhn
 * @N generische Open-Action in Basis-Klasse
 *
 * Revision 1.8  2011/10/27 17:08:03  willuhn
 * @C Berechnung der kuenftigen geplanten Termine von Dauerauftraegen in neue Klasse DauerauftragUtil verschoben, damit der Code wiederverwendet werden kann (fuer den Forecast-Provider von Dauerauftraegen)
 *
 * Revision 1.7  2011-10-06 10:49:23  willuhn
 * @N Termin-Provider fuer Umsaetze
 *
 * Revision 1.6  2011-01-20 17:12:39  willuhn
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