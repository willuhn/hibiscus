/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/LastschriftList.java,v $
 * $Revision: 1.1 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.UeberweisungDuplicate;
import de.willuhn.jameica.hbci.gui.action.UeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit Lastschrift gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class LastschriftList extends ContextMenu
{
	private I18N i18n	= null;

	  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Lastschriften.
	 */
	public LastschriftList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"), new UeberweisungNew()));
		addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausführen..."), new UeberweisungExecute()));
		addItem(new CheckedContextMenuItem(i18n.tr("Duplizieren"), new UeberweisungDuplicate()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new UeberweisungDelete()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neue Lastschrift..."), new UNeu()));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class UNeu extends LastschriftNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	} 
	
	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Lastschrift noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
	{
		
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     */
    public NotActiveMenuItem(String text, Action a)
    {
      super(text, a);
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
				Lastschrift u = (Lastschrift) o;
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
 * $Log: LastschriftList.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/