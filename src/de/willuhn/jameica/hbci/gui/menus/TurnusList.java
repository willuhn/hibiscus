/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/Attic/TurnusList.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/11/15 00:38:30 $
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
import de.willuhn.jameica.hbci.gui.action.TurnusDelete;
import de.willuhn.jameica.hbci.gui.action.TurnusNew;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Turnus-Objekten
 * angehaengt werden kann.
 */
public class TurnusList extends ContextMenu
{

  private I18N i18n;

  /**
   * Erzeugt das Kontext-Menu fuer eine Liste von Turnus-Objekten.
   */
  public TurnusList()
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new Item(i18n.tr("Bearbeiten..."),new TurnusNew()));
		addItem(new Item(i18n.tr("Löschen..."),new TurnusDelete()));

		addItem(ContextMenuItem.SEPARATOR);
		addItem(new ContextMenuItem(i18n.tr("Neuer Zahlungsturnus..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	// Das machen wir, um sicherzustellen, dass TurnusNew immer <null> erhaelt.
				new TurnusNew().handleAction(null);
      }
    }));
  }
  
  private class Item extends CheckedContextMenuItem
  { 
    /**
     * @param text
     * @param a
     */
    public Item(String text, Action a)
    {
      super(text, a);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
    	if (o != null)
    	{
				try
				{
					Turnus t = (Turnus) o;
					if (t.isInitial())
						return false;
				}
				catch (Exception e)
				{
					//ignore
				}
    	}
      return super.isEnabledFor(o);
    }

}

}


/**********************************************************************
 * $Log: TurnusList.java,v $
 * Revision 1.6  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.2  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 **********************************************************************/