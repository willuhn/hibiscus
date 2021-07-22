/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static Map<String,Date> cache = new HashMap<String,Date>();
  
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private String param = null;
  
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
    
    this.param = parameter != null ? parameter : "date";
    
    // Listener zur Ueberwachung der Aenderungen
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        store();
      }
    });


    // Wenn explizit ein Datum angegeben wurde, erzeugen wir keinen Vorschlag
    if (date != null)
      return;
    
    // Jetzt ermitteln wir, ob wir in der aktuellen Sitzung schonmal aufgerufen wurden
    // Wenn das der Fall ist, erzeugen wir keine Vorschlaege mehr, sondern uebernehmen
    // den letzten Wert
    if (cache.containsKey(this.param))
    {
      this.setValue(cache.get(this.param));
      return;
    }

    // OK, offensichtlich wurden wir das erste mal in der aktuellen Sitzung
    // aufgerufen. Wir ermitteln einen Vorschlag

    // a) In der Config schauen
    try
    {
      String s = settings.getString(this.param,null);
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
   * @see de.willuhn.jameica.gui.input.DateInput#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    super.setValue(value);
    this.store();
  }
  
  /**
   * Speichert das aktuelle Datum in den Settings und im Cache, damit
   * es beim naechsten Oeffnen der View wiederhergestellt wird.
   */
  private void store()
  {
    // aktuelles Datum in Config und Cache speichern speichern
    Date d = (Date) getValue();
    settings.setAttribute(this.param,d != null ? HBCI.DATEFORMAT.format(d) : null);
    cache.put(this.param,d);
  }
  
  
  /**
   * Liefert das zu verwendende Default-Datum.
   * @return das zu verwendende Default-Datum.
   */
  abstract Date getDefault();
}
