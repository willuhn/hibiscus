/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/KontoList.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/20 21:48:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.listener.KontoRefreshSaldo;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungCreate;
import de.willuhn.jameica.hbci.gui.views.KontoNeu;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Konto-Listen angehaengt werden kann.
 */
public class KontoList extends ContextMenu
{

	private I18N i18n;

	public KontoList()
	{
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new Listener()
		{
			public void handleEvent(Event event)
			{
				GUI.startView(KontoNeu.class.getName(),event.data);
			}
		}));

		addItem(new CheckedContextMenuItem(i18n.tr("Saldo aktualisieren..."), new Listener()
		{
			public void handleEvent(Event event)
			{
				new KontoRefreshSaldo().handleEvent(event);
				GUI.startView(GUI.getCurrentView().getClass().getName(),event.data);
			}
		}));

		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neue Überweisung..."), new UeberweisungCreate()));

		addItem(new ContextMenuItem(i18n.tr("Neues Konto..."), new Listener()
		{
			public void handleEvent(Event event)
			{
				GUI.startView(KontoNeu.class.getName(),null);
			}
		}));
	}

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/