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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Auswahlliste mit Zeitraeumen.
 */
public class RangeInput extends SelectInput
{
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private String param = null;
  private Input from = null;
  private Input to = null;
  private boolean inUpdate = false;
  
  /**
   * ct.
   * @param from Input-Feld, in das das Start-Datum nach der Auswahl uebernommen werden soll.
   * @param to Input-Feld, in das das End-Datum nach der Auswahl uebernommen werden soll.
   */
  public RangeInput(final Input from, final Input to)
  {
    this(from,to,null);
  }
  
  /**
   * ct.
   * @param from Input-Feld, in das das Start-Datum nach der Auswahl uebernommen werden soll.
   * @param to Input-Feld, in das das End-Datum nach der Auswahl uebernommen werden soll.
   * @param parameter Schluessel-Name, unter dem die Auswahl gespeichert wird.
   */
  public RangeInput(final Input from, final Input to, final String parameter)
  {
    super(Range.KNOWN,null);
    this.from = from;
    this.to = to;
    this.param = parameter != null ? parameter : "transferlist.filter.range";
    
    this.setPleaseChoose(i18n.tr("Bitte wählen..."));
    this.setName(i18n.tr("Zeitraum"));
    
    Range preset = Range.byId(settings.getString(this.param,null));
    if (preset != null)
    {
      this.setValue(preset);
      applyRange(preset);
    }
    

    // Wenn Von/Bis vorhanden sind, dann muessen wir den gespeicherten Range
    // loeschen, sobald der User etwas in Von/Bis geaendert hat. Denn wir wollen
    // ja nicht die Eingaben des Users wieder ueberschreiben
    if (from != null || to != null)
    {
      final Listener l = new Listener() {
        @Override
        public void handleEvent(Event event)
        {
          if (inUpdate) // Das wurde von uns selbst ausgeloest
            return;
          
          setValue(null);
          settings.setAttribute(param,(String)null);
          checkInvalidRange(from, to);
        }
      };
      
      if (from != null)
        from.addListener(l);
      if (to != null)
        to.addListener(l);
    }

    // Und ein Listener fuer uns selbst zum Speichern der Auswahl und Anwenden
    // der Aenderungen auf Von/Bis
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Range choosen = (Range) getValue();
        settings.setAttribute(param,choosen != null ? choosen.getId() : null);
        applyRange(choosen);
      }
    });
  }

  private void checkInvalidRange(Input from, Input to){
    if(from!=null && to!=null){
      Object fromValue = from.getValue();
      Object toValue = to.getValue();
      if(fromValue instanceof Date && toValue instanceof Date){
        if(((Date)toValue).before((Date)fromValue)){
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte prüfen Sie den Zeitraum - das Ende sollte nicht vor dem Anfang liegen."), StatusBarMessage.TYPE_ERROR));
        }
      }
    }
  }

  /**
   * Wendet den Range auf die Von- und Bis-Felder an.
   * @param range der Range. Kann null sein.
   */
  private void applyRange(Range range)
  {
    if (range == null)
      return;
    
    try
    {
      // Sicherstellen, dass der Range nicht resettet wird, wenn wir selbst
      // die Werte in Von/Bis eintragen
      this.inUpdate = true;
      
      if (this.from != null)
        this.from.setValue(range.getStart());
      
      if (this.to != null)
        this.to.setValue(range.getEnd());
    }
    finally
    {
      this.inUpdate = false;
    }
  }

}


