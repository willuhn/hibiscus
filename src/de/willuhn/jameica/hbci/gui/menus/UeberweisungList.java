/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/UeberweisungList.java,v $
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

import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungCreate;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungDuplicate;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungExecute;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Kontext-Menu, welches an Listen mit Ueberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class UeberweisungList extends ContextMenu
{
	private I18N i18n	= null;
	public UeberweisungList()
	{
		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		addItem(new DuplicateMenuItem(i18n.tr("Jetzt ausführen..."), new UeberweisungExecute()));
		addItem(new CheckedContextMenuItem(i18n.tr("Duplizieren"), new UeberweisungDuplicate()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neue Überweisung..."), new UeberweisungCreate()));
		
	}

	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Ueberweisung nocht nicht ausgefuehrt wurde.
   */
  private class DuplicateMenuItem extends ContextMenuItem
	{
		
    public DuplicateMenuItem()
    {
      super();
    }

    public DuplicateMenuItem(String text, Listener l)
    {
      super(text, l);
    }

	  /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
    	if (o == null)
    		return false;
    	try
    	{
    		Ueberweisung u = (Ueberweisung) o;
    		return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }

}
}


/**********************************************************************
 * $Log: UeberweisungList.java,v $
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/