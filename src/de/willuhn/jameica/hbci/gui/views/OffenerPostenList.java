/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/OffenerPostenList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
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
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.OffenerPostenNew;
import de.willuhn.jameica.hbci.gui.controller.OffenerPostenControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Offenen Posten an.
 */
public class OffenerPostenList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Offene Posten"));
		
		OffenerPostenControl control = new OffenerPostenControl(this);
		
		try {

			control.getOPListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),2);
      buttons.addButton(i18n.tr("Zurück"),new Back());
			buttons.addButton(i18n.tr("neuer Eintrag in OP-Liste"),new OffenerPostenNew(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading op list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der offenen Posten."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: OffenerPostenList.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/