/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/DauerauftragListe.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/13 23:08:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Zeigt eine Liste mit den vorhandenen Ueberweisungen an.
 */
public class DauerauftragListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
		
		UeberweisungControl control = new UeberweisungControl(this);
		
		try {

			control.getUeberweisungListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(i18n.tr("neue Überweisung"),control);

		}
		catch (Exception e)
		{
			Logger.error("error while loading ueberweisung list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Überweisungen."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: DauerauftragListe.java,v $
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/