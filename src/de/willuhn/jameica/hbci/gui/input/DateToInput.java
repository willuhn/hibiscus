/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/DateToInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/08/05 11:21:59 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.util.I18N;

/**
 * Vorkonfigueriertes Eingabefeld fuer ein End-Datum.
 */
public class DateToInput extends DateInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   */
  public DateToInput()
  {
    this(null);
  }
  
  /**
   * ct.
   * @param date
   */
  public DateToInput(Date date)
  {
    super(init(date),HBCI.DATEFORMAT);
    this.setName(i18n.tr("End-Datum"));
    this.setComment(i18n.tr("Spätestes Datum"));
    this.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // aktuelles Datum speichern
        Date d = (Date) getValue();
        Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
        settings.setAttribute("transferlist.filter.to",d != null ? HBCI.DATEFORMAT.format(d) : null);
      }
    });
  }
  
  /**
   * Initialisiert das Datum.
   * @param date das Vorgabe-Datum.
   * @return das zu verwendende Datum.
   */
  private static Date init(Date date)
  {
    // a) Wir haben ein explizites Datum
    if (date != null)
      return date;
    
    // b) Datum in den Einstellungen vorhanden?
    try
    {
      Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
      String s = settings.getString("transferlist.filter.to",null);
      if (s != null && s.length() > 0)
        return HBCI.DATEFORMAT.parse(s);
    }
    catch (Exception e)
    {
      // ignore
    }
    
    // c) 31.12. des aktuellen Jahres
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH,Calendar.DECEMBER);
    cal.set(Calendar.DATE,31);

    return DateUtil.endOfDay(cal.getTime());
  }
}



/**********************************************************************
 * $Log: DateToInput.java,v $
 * Revision 1.1  2011/08/05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/