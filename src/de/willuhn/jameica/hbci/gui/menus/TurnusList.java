/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/Attic/TurnusList.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/11/13 17:12:15 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.TurnusDelete;
import de.willuhn.jameica.hbci.gui.action.TurnusNew;
import de.willuhn.jameica.system.Application;
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

		addItem(new CheckedContextMenuItem(i18n.tr("Bearbeiten..."),new TurnusNew()));
		addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."),new TurnusDelete()));

		// TODO: TurnusNew
		//addItem(new CheckedContextMenuItem(i18n.tr("Neu..."),new TurnusDelete()));
  }

}


/**********************************************************************
 * $Log: TurnusList.java,v $
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