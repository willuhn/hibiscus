/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/SammelLastschriftList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/03/02 00:22:05 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SammelLastschriften gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SammelLastschriftList extends ContextMenu
{
	private I18N i18n	= null;

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Lastschriften.
	 */
	public SammelLastschriftList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"), new SammelLastschriftNew()));
		addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausführen..."), new SammelLastschriftExecute()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new SammelLastschriftDelete()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neue Sammel-Lastschrift..."), new SNeu()));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Sammel-Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class SNeu extends SammelLastschriftNew
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
				SammelLastschrift u = (SammelLastschrift) o;
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
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 18:40:49  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/