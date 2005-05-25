/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/Attic/OffenerPostenList.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
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
import de.willuhn.jameica.hbci.gui.action.OffenerPostenDelete;
import de.willuhn.jameica.hbci.gui.action.OffenerPostenNew;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von offenen Posten
 * angehaengt werden kann.
 */
public class OffenerPostenList extends ContextMenu
{

	private I18N i18n;

	/**
	 * Erzeugt das Kontext-Menu fuer eine Liste von offenen Posten.
	 */
	public OffenerPostenList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new SingleItem(i18n.tr("Öffnen"),new OffenerPostenNew()));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new OffenerPostenDelete()));
		addItem(ContextMenuItem.SEPARATOR);
    addItem(new ContextMenuItem(i18n.tr("Offenen Posten anlegen..."), new OPNeu()));
	}

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class SingleItem extends CheckedContextMenuItem
  {
    /**
     * @param text
     * @param action
     */
    private SingleItem(String text, Action action)
    {
      super(text,action);
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof OffenerPosten[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  /**
   * Ueberschreiben wir, um <b>grundsaetzlich</b> einen neuen offenen Posten
   * anzulegen - auch wenn der Focus auf einem existierenden liegt.
   */
  private class OPNeu extends OffenerPostenNew
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
 * $Log: OffenerPostenList.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/