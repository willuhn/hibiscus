/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzDetail.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/22 22:36:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Detail-Ansicht eines Umsatzes.
 */
public class UmsatzDetail implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    // Falls die Aktion aus einem Tree heraus aufgerufen wurde
    // koennte es sich um einen "UmsatzTyp" handeln. Den ignorieren
    // wir.
    if (!(context instanceof Umsatz))
      return;
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UmsatzDetail.class,context);
  }

}


/**********************************************************************
 * $Log: UmsatzDetail.java,v $
 * Revision 1.3  2007/03/22 22:36:42  willuhn
 * @N Contextmenu in Trees
 * @C Kategorie-Baum in separates TreePart ausgelagert
 *
 * Revision 1.2  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 **********************************************************************/