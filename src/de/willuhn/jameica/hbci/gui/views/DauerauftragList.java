/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DauerauftragList.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/04/08 15:19:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.KontoFetchDauerauftraege;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
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
		GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
		
		final DauerauftragControl control = new DauerauftragControl(this);
		
    control.getDauerauftragListe().paint(getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Daueraufträge abrufen..."), 	new KontoFetchDauerauftraege(),null,false,"mail-send-receive.png");
    buttons.addButton(i18n.tr("Neuer Dauerauftrag"),				new DauerauftragNew(),null,true,"text-x-generic.png");

    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: DauerauftragList.java,v $
 * Revision 1.9  2011/04/08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.8  2011-03-07 10:33:53  willuhn
 * @N BUGZILLA 999
 **********************************************************************/