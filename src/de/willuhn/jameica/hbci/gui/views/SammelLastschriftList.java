/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/28 16:28:24 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Sammel-Lastschriften an.
 */
public class SammelLastschriftList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Lastschriften"));
		
		SammelLastschriftControl control = new SammelLastschriftControl(this);
		
		try {

			control.getListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addButton(i18n.tr("neue Sammel-Lastschrift"),new SammelLastschriftNew(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading sammellastschrift list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Sammel-Lastschriften."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/