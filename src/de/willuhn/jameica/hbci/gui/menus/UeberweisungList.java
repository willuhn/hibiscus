/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/UeberweisungList.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/08/18 23:13:51 $
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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungCreate;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungDuplicate;
import de.willuhn.jameica.hbci.gui.listener.UeberweisungExecute;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Kontext-Menu, welches an Listen mit Ueberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class UeberweisungList extends ContextMenu
{
	private I18N i18n	= null;

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Ueberweisungen.
	 */
	public UeberweisungList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new CheckedContextMenuItem(i18n.tr("Öffnen"), new Listener()
    {
      public void handleEvent(Event event)
      {
      	GUI.startView(UeberweisungNeu.class.getName(),event.data);
      }
    }));
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
		
    /**
     * ct.
     */
    public DuplicateMenuItem()
    {
      super();
    }

    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param l auszufuehrender Listener.
     */
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
 * Revision 1.5  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.4  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.3  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/20 23:31:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/