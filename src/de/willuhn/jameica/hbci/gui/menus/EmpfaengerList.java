/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/EmpfaengerList.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/10/18 23:38:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNeu;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNeu;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Empfaenger-Adressen
 * angehaengt werden kann.
 */
public class EmpfaengerList extends ContextMenu
{

	private I18N i18n;

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von Empfaengern.
	 */
	public EmpfaengerList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new EmpfaengerNeu()));
		addItem(new CheckedContextMenuItem(i18n.tr("Neue Überweisung mit diesem Empfänger..."), new UeberweisungNeu()));
		addItem(ContextMenuItem.SEPARATOR);

		addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new EmpfaengerNeu()));
	}

}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
 * Revision 1.6  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.5  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.4  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.3  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/