/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DauerauftragList.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/09/12 15:28:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.KontoFetchDauerauftraege;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportDauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Dauerauftraegen an.
 */
public class DauerauftragList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    DauerauftragControl control = new DauerauftragControl(this);
    
    final de.willuhn.jameica.hbci.gui.parts.DauerauftragList table = control.getDauerauftragListe();
    final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportDauerauftrag(table));
    table.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        print.setEnabled(table.getSelection() != null);
      }
    });

    GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
    GUI.getView().addPanelButton(print);
		
    table.paint(getParent());
    print.setEnabled(table.getSelection() != null); // einmal initial ausloesen

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Daueraufträge abrufen..."), 	new KontoFetchDauerauftraege(),null,false,"mail-send-receive.png");
    buttons.addButton(i18n.tr("Neuer Dauerauftrag"),				new DauerauftragNew(),null,true,"text-x-generic.png");

    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: DauerauftragList.java,v $
 * Revision 1.11  2011/09/12 15:28:00  willuhn
 * @N Enabled-State live uebernehmen - nicht erst beim Mouse-Over
 *
 * Revision 1.10  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.9  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.8  2011-03-07 10:33:53  willuhn
 * @N BUGZILLA 999
 **********************************************************************/