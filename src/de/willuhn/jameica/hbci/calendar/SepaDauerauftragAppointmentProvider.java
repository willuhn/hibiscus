/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer anstehende SEPA-Dauerauftraege.
 */
public class SepaDauerauftragAppointmentProvider extends AbstractAppointmentProvider<SepaDauerauftrag>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  AbstractHibiscusAppointment createAppointment(Schedule<SepaDauerauftrag> schedule)
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
    private MyAppointment(Schedule<SepaDauerauftrag> schedule)
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
        SepaDauerauftrag t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("SEPA-Dauerauftrag: {0} {1} an {2}\n{3}\n\n{4}\n\nKonto: {5}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName(),TurnusHelper.createBezeichnung(t.getTurnus()),VerwendungszweckUtil.toString(t,"\n"),k.getLongName());
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
        SepaDauerauftrag t = this.schedule.getContext();
        Konto k = t.getKonto();
        return i18n.tr("{0} {1} an {2}",HBCI.DECIMALFORMAT.format(t.getBetrag()),k.getWaehrung(),t.getGegenkontoName());
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("SEPA-Dauerauftrag");
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
