/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer offene SEPA-Sammellastschriften.
 */
public class SepaSammelLastschriftAppointmentProvider extends AbstractAppointmentProvider<SepaSammelLastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  AbstractHibiscusAppointment createAppointment(Schedule<SepaSammelLastschrift> schedule)
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
    private MyAppointment(Schedule<SepaSammelLastschrift> schedule)
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
        SepaSammelLastschrift t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0}SEPA-Sammellastschrift: {1} {2} einziehen\n\n{3}\n\nKonto: {4}",
                       (this.schedule.isPlanned() ? (i18n.tr("Geplant") + ":\n") : ""),
                       HBCI.DECIMALFORMAT.format(t.getSumme()),
                       k.getWaehrung(),
                       t.getBezeichnung(),
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
        SepaSammelLastschrift t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} {2}",HBCI.DECIMALFORMAT.format(t.getSumme()),k.getWaehrung(),t.getBezeichnung());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("SEPA-Sammellastschrift");
      }
    }
  }
}
