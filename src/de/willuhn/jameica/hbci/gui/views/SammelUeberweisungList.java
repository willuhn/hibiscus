/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelUeberweisungList.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/05/06 23:11:23 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Sammel-Lastschriften an.
 */
public class SammelUeberweisungList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Überweisungen"));
		
		SammelUeberweisungControl control = new SammelUeberweisungControl(this);
		
		try {

			control.getListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),3);
      buttons.addButton(new Back(false));
      buttons.addButton(i18n.tr("Importieren..."),new SammelUeberweisungImport(),null,false,"document-open.png");
			buttons.addButton(i18n.tr("Neue Sammel-Überweisung"),new SammelUeberweisungNew(),null,true,"text-x-generic.png");

		}
		catch (Exception e)
		{
			Logger.error("error while loading sammeltransfer list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Sammel-Überweisungen."));
		}
  }
}


/**********************************************************************
 * $Log: SammelUeberweisungList.java,v $
 * Revision 1.5  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.4  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.3  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.2  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/