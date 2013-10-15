/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/UmsatzAppointmentProvider.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/02/20 17:03:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.calendar;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.schedule.Schedule;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Termin-Providers fuer die Umsaetze.
 */
public class UmsatzAppointmentProvider extends AbstractAppointmentProvider<Umsatz>
{
  /**
   * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider#createAppointment(de.willuhn.jameica.hbci.schedule.Schedule)
   */
  AbstractHibiscusAppointment createAppointment(Schedule<Umsatz> schedule)
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
    private MyAppointment(Schedule<Umsatz> schedule)
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
        Umsatz t      = this.schedule.getContext();
        Konto k       = t.getKonto();
        double betrag = t.getBetrag();
        String rel    = i18n.tr(betrag >= 0.0d ? "von" : "an");
        String zweck  = VerwendungszweckUtil.toString(t,"\n");
        String name   = StringUtils.trimToEmpty(t.getGegenkontoName());

        betrag = Math.abs(betrag);
        
        return i18n.tr("Umsatz: {0} {1} {2} {3}\n\n{4}\n\nKonto: {5}",HBCI.DECIMALFORMAT.format(betrag),k.getWaehrung(),rel,name,zweck,k.getLongName());
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
        Umsatz t      = this.schedule.getContext();
        String curr   = t.getKonto().getWaehrung();
        double betrag = t.getBetrag();
        String name   = t.getGegenkontoName();
        String usage  = t.getZweck();
        if (StringUtils.trimToNull(name) != null)
        {
          // Wenn wir einen Gegenkontonamen haben, nehmen wir den
          return i18n.tr("{0} {1} {2}",HBCI.DECIMALFORMAT.format(betrag),curr,name);
        }
        else if (StringUtils.trimToNull(usage) != null)
        {
          // andernfalls den Verwendungszweck
          return i18n.tr("{0} {1} {2}",HBCI.DECIMALFORMAT.format(betrag),curr,usage);
        }
        else
        {
          // Wenn wir auch den nicht haben, dann nur den Betrag
          return i18n.tr("{0} {1}",HBCI.DECIMALFORMAT.format(betrag),curr);
        }
      }
      catch (RemoteException re)
      {
        Logger.error("unable to build name",re);
        return i18n.tr("Umsatz");
      }
    }

    /**
     * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider.AbstractHibiscusAppointment#getColor()
     */
    public RGB getColor()
    {
      // Ueberschrieben, weil wir die Farbe hier abhaengig von Soll/Haben machen
      try
      {
        Umsatz t      = this.schedule.getContext();
        double betrag = t.getBetrag();
        if (betrag > 0.0d)
          return Settings.getBuchungHabenForeground().getRGB();
        else if (betrag < 0.0d)
          return Settings.getBuchungSollForeground().getRGB();
      }
      catch (Exception e)
      {
        Logger.error("unable to detect color",e);
      }
      return Color.BLACK.getSWTColor().getRGB();
    }
    
    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#hasAlarm()
     */
    public boolean hasAlarm()
    {
      // Alarm brauchen wir hier generell nicht
      return false;
    }
  }
}



/**********************************************************************
 * $Log: UmsatzAppointmentProvider.java,v $
 * Revision 1.3  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 * Revision 1.2  2012/02/05 12:03:43  willuhn
 * @N generische Open-Action in Basis-Klasse
 *
 * Revision 1.1  2011-10-06 10:49:24  willuhn
 * @N Termin-Provider fuer Umsaetze
 *
 **********************************************************************/