/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/EmpfaengerList.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/01/19 00:16:04 $
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
import de.willuhn.jameica.hbci.gui.action.EmpfaengerDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
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

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new EmpfaengerNew()));
		addItem(new CheckedContextMenuItem(i18n.tr("Neue Überweisung mit diesem Empfänger..."), new UeberweisungNew()));
		addItem(new CheckedContextMenuItem(i18n.tr("Neue Lastschrift von diesem Konto einziehen..."), new LastschriftNew()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new EmpfaengerDelete()));
		addItem(ContextMenuItem.SEPARATOR);

		addItem(new ContextMenuItem(i18n.tr("Neue Adresse..."), new EmpfaengerNew()));
	}

}


/**********************************************************************
 * $Log: EmpfaengerList.java,v $
 * Revision 1.9  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.8  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.7  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
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