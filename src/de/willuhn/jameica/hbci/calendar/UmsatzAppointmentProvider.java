/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/calendar/UmsatzAppointmentProvider.java,v $
 * $Revision: 1.2 $
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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.calendar.AbstractAppointment;
import de.willuhn.jameica.gui.calendar.Appointment;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Termin-Providers fuer die Umsaetze.
 */
public class UmsatzAppointmentProvider implements AppointmentProvider
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
      DBIterator list = service.createList(Umsatz.class);
      if (from != null) list.addFilter("valuta >= ?", new Object[]{new java.sql.Date(DateUtil.startOfDay(from).getTime())});
      if (to   != null) list.addFilter("valuta <= ?", new Object[]{new java.sql.Date(DateUtil.endOfDay(to).getTime())});
      list.setOrder("ORDER BY " + service.getSQLTimestamp("valuta"));

      List<Appointment> result = new LinkedList<Appointment>();
      while (list.hasNext())
        result.add(new MyAppointment((Umsatz) list.next()));
      
      return result;
    }
    catch (Exception e)
    {
      Logger.error("unable to load data",e);
    }
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.calendar.AppointmentProvider#getName()
   */
  public String getName()
  {
    return i18n.tr("Umsätze");
  }
  
  /**
   * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
   */
  private class MyAppointment extends AbstractAppointment
  {
    private Umsatz t = null;
    
    /**
     * ct.
     * @param t der Umsatz.
     */
    private MyAppointment(Umsatz t)
    {
      this.t = t;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#execute()
     */
    public void execute() throws ApplicationException
    {
      new Open().handleAction(this.t);
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.Appointment#getDate()
     */
    public Date getDate()
    {
      try
      {
        return t.getValuta();
      }
      catch (Exception e)
      {
        Logger.error("unable to read date",e);
      }
      return null;
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
     */
    public String getDescription()
    {
      try
      {
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
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getColor()
     */
    public RGB getColor()
    {
      try
      {
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
      return Color.WIDGET_FG.getSWTColor().getRGB();
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getUid()
     */
    public String getUid()
    {
      try
      {
        return this.t.getClass().getName() + "." + t.getID();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create uid",re);
        return super.getUid();
      }
    }

    /**
     * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#hasAlarm()
     */
    public boolean hasAlarm()
    {
      return false;
    }
  }
}



/**********************************************************************
 * $Log: UmsatzAppointmentProvider.java,v $
 * Revision 1.2  2012/02/05 12:03:43  willuhn
 * @N generische Open-Action in Basis-Klasse
 *
 * Revision 1.1  2011-10-06 10:49:24  willuhn
 * @N Termin-Provider fuer Umsaetze
 *
 **********************************************************************/