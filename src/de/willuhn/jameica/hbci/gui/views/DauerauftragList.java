/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DauerauftragList.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/01/20 10:51:46 $
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
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.KontoFetchDauerauftraege;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Dauerauftraegen an.
 */
public class DauerauftragList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Daueraufträge"));
		
		final DauerauftragControl control = new DauerauftragControl(this);
		
		try {

			control.getDauerauftragListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),3);
	    buttons.addButton(new Back(false));
			buttons.addButton(i18n.tr("Existierende Daueraufträge abrufen"), 	new KontoFetchDauerauftraege());
			buttons.addButton(i18n.tr("Neuer Dauerauftrag"),									new DauerauftragNew(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading dauerauftrag list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Daueraufträge."));
		}
  }
}


/**********************************************************************
 * $Log: DauerauftragList.java,v $
 * Revision 1.6  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.5  2006/08/07 14:45:18  willuhn
 * @B typos
 *
 * Revision 1.4  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.3  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.10  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.6  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.2  2004/07/13 23:26:14  willuhn
 * @N Views fuer Dauerauftrag
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/