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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer offene SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungAppointmentProvider extends AbstractAppointmentProvider<SepaSammelUeberweisung>
{
  @Override
  AbstractHibiscusAppointment createAppointment(Schedule<SepaSammelUeberweisung> schedule)
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
    private MyAppointment(Schedule<SepaSammelUeberweisung> schedule)
    {
      super(schedule);
    }

    @Override
    public String getDescription()
    {
      try
      {
        SepaSammelUeberweisung t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0}SEPA-Sammel�berweisung: {1} {2} �berweisen\n\n{3}\n\nKonto: {4}",
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

    @Override
    public String getName()
    {
      try
      {
        SepaSammelUeberweisung t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} {2}",HBCI.DECIMALFORMAT.format(t.getSumme()),k.getWaehrung(),t.getBezeichnung());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("SEPA-Sammel�berweisung");
      }
    }
  }
}
