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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer offene SEPA-Ueberweisungen.
 */
public class AuslandsUeberweisungAppointmentProvider extends AbstractAppointmentProvider<AuslandsUeberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  AbstractHibiscusAppointment createAppointment(Schedule<AuslandsUeberweisung> schedule)
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
     * @param schedule der Auftrag.
     */
    private MyAppointment(Schedule<AuslandsUeberweisung> schedule)
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
        AuslandsUeberweisung t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0}SEPA-Überweisung: {1} {2} an {3} überweisen\n\n{4}\n\nKonto: {5}",
                       (this.schedule.isPlanned() ? (i18n.tr("Geplant") + ":\n") : ""),
                       HBCI.DECIMALFORMAT.format(t.getBetrag()),
                       k.getWaehrung(),
                       t.getGegenkontoName(),
                       VerwendungszweckUtil.toString(t,"\n"),
                       k.getLongName());
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
        AuslandsUeberweisung t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} an {2}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("SEPA-Überweisung");
      }
    }
  }
}



/**********************************************************************
 * $Log: AuslandsUeberweisungAppointmentProvider.java,v $
 * Revision 1.8  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 * Revision 1.7  2012/02/05 12:03:43  willuhn
 * @N generische Open-Action in Basis-Klasse
 *
 * Revision 1.6  2011/12/13 23:10:21  willuhn
 * @N BUGZILLA 1162
 *
 * Revision 1.5  2011-10-06 10:49:23  willuhn
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