/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/Attic/SammelLastBuchungList.java,v $
 * $Revision: 1.2 $
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
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit Buchungen innerhalb von Sammellastschriften gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SammelLastBuchungList extends ContextMenu
{
	private I18N i18n	= null;
  private SammelLastschrift lastschrift = null;

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Buchungen in einer Sammellastschrift.
   * @param lastschrift Zugehoerige Sammel-Lastschrift.
   * Die Sammel-Lastschrift wird fuer den Menu-Eintrag "Neue Buchung..." benoetigt.
   * Um eine neue Buchung zu erzeugen, muss die zugehoerige Sammellastschrift bekannt sein.
	 */
	public SammelLastBuchungList(SammelLastschrift lastschrift)
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.lastschrift = lastschrift;

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"), new SammelLastBuchungNew()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new NotActiveMenuItem(i18n.tr("Löschen..."), new SammelLastBuchungDelete()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new NotActiveMenuItem(i18n.tr("Neue Buchung..."), new BNeu()));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Buchung.
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class BNeu extends SammelLastBuchungNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(lastschrift);
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
				SammelLastBuchung u = (SammelLastBuchung) o;
    		return !u.getSammelLastschrift().ausgefuehrt();
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
 * $Log: SammelLastBuchungList.java,v $
 * Revision 1.2  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 **********************************************************************/