/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/LastschriftList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/19 00:16:04 $
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Lastschrift an.
 */
public class LastschriftList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Lastschrift"));
		
		LastschriftControl control = new LastschriftControl(this);
		
		try {

			control.getLastschriftListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addButton(i18n.tr("neue Lastschrift"),new LastschriftNew());

		}
		catch (Exception e)
		{
			Logger.error("error while loading lastschrift list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Lastschriften."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: LastschriftList.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/