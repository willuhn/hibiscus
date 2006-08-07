/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftList.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/08/07 14:31:59 $
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
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftImport;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Sammel-Lastschriften an.
 */
public class SammelLastschriftList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Lastschriften"));
		
		SammelLastschriftControl control = new SammelLastschriftControl(this);
		
		try {

			control.getListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),3);
      buttons.addButton(i18n.tr("Zurück"),new Back());
      buttons.addButton(i18n.tr("Importieren..."),new SammelLastschriftImport());
			buttons.addButton(i18n.tr("Neue Sammel-Lastschrift"),new SammelLastschriftNew(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading sammellastschrift list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Sammel-Lastschriften."));
		}
  }
}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.6  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.5  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.4  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/