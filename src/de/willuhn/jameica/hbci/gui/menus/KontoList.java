/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/KontoList.java,v $
 * $Revision: 1.5 $
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
import de.willuhn.jameica.hbci.gui.action.KontoNeu;
import de.willuhn.jameica.hbci.gui.action.KontoRefreshSaldo;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNeu;
import de.willuhn.jameica.hbci.gui.action.UmsatzListe;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Konto-Listen angehaengt werden kann.
 */
public class KontoList extends ContextMenu
{

	private I18N i18n;

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Konten.
	 */
	public KontoList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"),new KontoNeu()));
		addItem(new CheckedContextMenuItem(i18n.tr("Kontoauszüge anzeigen..."), new UmsatzListe()));
		addItem(new CheckedContextMenuItem(i18n.tr("Saldo aktualisieren..."), new KontoRefreshSaldo()));

		addItem(ContextMenuItem.SEPARATOR);

		addItem(new ContextMenuItem(i18n.tr("Neue Überweisung..."), new UeberweisungNeu()));
		addItem(new ContextMenuItem(i18n.tr("Neues Konto..."), new KNeu()));
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> ein neues Konto
	 * anzulegen - auch wenn der Focus auf einem existierenden liegt.
	 */
	private class KNeu extends KontoNeu
	{
		/**
		 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
		 */
		public void handleAction(Object context) throws ApplicationException
		{
			super.handleAction(null);
		}
	} 

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.5  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.4  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/