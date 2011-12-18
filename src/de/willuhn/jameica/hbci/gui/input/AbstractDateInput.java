/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/AbstractDateInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/12/18 23:20:20 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
  private static Map<String,Date> cache = new HashMap<String,Date>();
  
  /**
   * ct.
   * @param date
   */
  public AbstractDateInput(Date date)
  {
    this(date,null);
  }

  /**
   * ct.
   * @param date
   * @param parameter Schluessel-Name, unter dem die Eingabe in der Session gecached werden soll.
   */
  public AbstractDateInput(Date date, final String parameter)
  {
    super(date,HBCI.DATEFORMAT);
    this.setName(i18n.tr("Datum"));
    
    final Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    final String param = parameter != null ? parameter : "date";
    
    // Listener zur Ueberwachung der Aenderungen
    this.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // aktuelles Datum in Config und Cache speichern speichern
        Date d = (Date) getValue();
        settings.setAttribute(param,d != null ? HBCI.DATEFORMAT.format(d) : null);
        cache.put(param,d);
      }
    });


    // Wenn explizit ein Datum angegeben wurde, erzeugen wir keinen Vorschlag
    if (date != null)
      return;
    
    // Jetzt ermitteln wir, ob wir in der aktuellen Sitzung schonmal aufgerufen wurden
    // Wenn das der Fall ist, erzeugen wir keine Vorschlaege mehr, sondern uebernehmen
    // den letzten Wert
    if (cache.containsKey(param))
    {
      this.setValue(cache.get(param));
      return;
    }

    // OK, offensichtlich wurden wir das erste mal in der aktuellen Sitzung
    // aufgerufen. Wir ermitteln einen Vorschlag

    // a) In der Config schauen
    try
    {
      String s = settings.getString(param,null);
      if (s != null && s.length() > 0)
      {
        this.setValue(HBCI.DATEFORMAT.parse(s));
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error(e.getMessage(),e);
    }

    // b) Vorgabewert holen
    this.setValue(this.getDefault());
  }
  
  /**
   * Liefert das zu verwendende Default-Datum.
   * @return das zu verwendende Default-Datum.
   */
  abstract Date getDefault();
}



/**********************************************************************
 * $Log: AbstractDateInput.java,v $
 * Revision 1.4  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.3  2011-08-05 12:02:11  willuhn
 * @B Konstruktor falsch
 *
 * Revision 1.2  2011-08-05 11:50:48  willuhn
 * @N Vorschlaege nur beim ersten Mal in der Sitzung ausrechnen - danach das behalten, was der User eingegeben hat - auch wenn es NULL ist
 *
 * Revision 1.1  2011-08-05 11:34:39  willuhn
 * @N Gemeinsame Basis-Klasse
 *
 * Revision 1.1  2011-08-05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/