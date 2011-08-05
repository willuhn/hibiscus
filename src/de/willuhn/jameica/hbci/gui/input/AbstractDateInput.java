/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/AbstractDateInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/08/05 11:34:39 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer DateFromInput und DateToInput
 */
public abstract class AbstractDateInput extends DateInput
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   */
  public AbstractDateInput()
  {
    this(null);
  }
  
  /**
   * ct.
   * @param date
   */
  public AbstractDateInput(Date date)
  {
    super(date,HBCI.DATEFORMAT);

    // Checken, ob wir was in der Config haben
    if (date == null)
    {
      try
      {
        Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
        String s = settings.getString(getParameter(),null);
        if (s != null && s.length() > 0)
          date = HBCI.DATEFORMAT.parse(s);
      }
      catch (Exception e)
      {
        Logger.error(e.getMessage(),e);
      }
    }

    // Dann halt das Default-Datum
    if (date == null)
      date = this.getDefault();
    
    this.setValue(date);
    
    this.setName(i18n.tr("Datum"));
    this.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // aktuelles Datum speichern
        Date d = (Date) getValue();
        Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
        settings.setAttribute(getParameter(),d != null ? HBCI.DATEFORMAT.format(d) : null);
      }
    });
  }
  
  /**
   * Liefert den Namen des Parameters fuer die Config-Datei.
   * @return Name des Parameters.
   */
  abstract String getParameter();
  
  /**
   * Liefert das zu verwendende Default-Datum.
   * @return das zu verwendende Default-Datum.
   */
  abstract Date getDefault();
}



/**********************************************************************
 * $Log: AbstractDateInput.java,v $
 * Revision 1.1  2011/08/05 11:34:39  willuhn
 * @N Gemeinsame Basis-Klasse
 *
 * Revision 1.1  2011-08-05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/