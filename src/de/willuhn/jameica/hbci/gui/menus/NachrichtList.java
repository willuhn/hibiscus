/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/NachrichtList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/09 17:26:56 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.gui.action.NachrichtDelete;
import de.willuhn.jameica.hbci.gui.action.NachrichtMarkRead;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von System-Nachrichten
 * angehaengt werden kann.
 */
public class NachrichtList extends ContextMenu
{

	private I18N i18n;

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von Nachrichten.
	 */
	public NachrichtList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    addItem(new ContextMenuItem(i18n.tr("Als gelesen markieren"), new NachrichtMarkRead()));
    addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new NachrichtDelete()));
	}
}


/**********************************************************************
 * $Log: NachrichtList.java,v $
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/