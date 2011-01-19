/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/Termine.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/01/19 23:19:37 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.util.List;

import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.jameica.gui.calendar.AppointmentProviderRegistry;
import de.willuhn.jameica.gui.calendar.ReminderCalendarPart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Zeigt die anstehenden Termine in Hibiscus an.
 */
public class Termine extends ReminderCalendarPart
{
  /**
   * ct.
   */
  public Termine()
  {
    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);

    List<AppointmentProvider> list = AppointmentProviderRegistry.getAppointmentProviders(plugin);
    for (AppointmentProvider p:list)
    {
      addAppointmentProvider(p);
    }
  }
}



/**********************************************************************
 * $Log: Termine.java,v $
 * Revision 1.6  2011/01/19 23:19:37  willuhn
 * @N Code zum Suchen nach AppointmentProvidern in "AppointmentProviderRegistry" verschoben
 *
 * Revision 1.5  2011-01-17 17:31:11  willuhn
 * @C Reminder-Zeug
 *
 * Revision 1.4  2011-01-14 17:33:41  willuhn
 * @N Erster Code fuer benutzerdefinierte Erinnerungen via Reminder-Framework
 *
 * Revision 1.3  2010-11-25 21:55:57  willuhn
 * @C Nur Termine aus Hibiscus anzeigen
 *
 * Revision 1.2  2010-11-22 00:52:53  willuhn
 * @C Appointment-Inner-Class darf auch private sein
 *
 * Revision 1.1  2010-11-19 18:37:19  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 **********************************************************************/