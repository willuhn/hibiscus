/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/AuslandsUeberweisungAppointmentProvider.java,v $
 * $Revision: 1.7 $
 * $Date: 2012/02/05 12:03:43 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer offene SEPA-Ueberweisungen.
 */
public class AuslandsUeberweisungAppointmentProvider extends AbstractTransferAppointmentProvider<AuslandsUeberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractTransferAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.rmi.Terminable, java.util.Date)
   */
  AbstractTransferAppointment createAppointment(AuslandsUeberweisung t, Date date)
  {
    return new MyAppointment(t,date);
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("SEPA-Überweisungen");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment extends AbstractTransferAppointment
  {
    /**
     * ct.
     * @param t die SEPA-Ueberweisung.
     * @param date ggf abweichender Termin.
     */
    private MyAppointment(AuslandsUeberweisung t, Date date)
    {
      super(t,date);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
        Konto k = t.getKonto();
        return i18n.tr("{0}SEPA-Überweisung: {1} {2} an {3} überweisen\n\n{4}\n\nKonto: {5}",
                       (this.date != null ? (i18n.tr("Geplant") + ":\n") : ""),
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