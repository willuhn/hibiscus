/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Auswahlliste mit Zeitraeumen.
 */
public class RangeInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @param from Input-Feld, in das das Start-Datum nach der Auswahl uebernommen werden soll.
   * @param to Input-Feld, in das das End-Datum nach der Auswahl uebernommen werden soll.
   */
  public RangeInput(final Input from, final Input to)
  {
    super(Range.KNOWN,null);
    this.setPleaseChoose(i18n.tr("Bitte wählen..."));
    this.setName(i18n.tr("Zeitraum"));
    
    this.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Range choosen = (Range) getValue();
        if (choosen == null)
          return;

        if (from != null)
          from.setValue(choosen.getStart());
        
        if (to != null)
          to.setValue(choosen.getEnd());
      }
    });
  }

}


