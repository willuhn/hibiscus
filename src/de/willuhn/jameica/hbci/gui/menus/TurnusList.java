/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/Attic/TurnusList.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/26 23:47:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.hbci.HBCI;
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

    // TODO TurnusMenu
  }

}


/**********************************************************************
 * $Log: TurnusList.java,v $
 * Revision 1.2  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.6  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.5  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.4  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.3  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.1  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 **********************************************************************/