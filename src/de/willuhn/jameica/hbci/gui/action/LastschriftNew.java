/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftNew.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/04/22 16:21:27 $
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
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Lastschrift.
 */
public class LastschriftNew implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder eine Lastschrift angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Lastschrift
   * vorausgefuellt oder die uebergebene Lastschrift geladen.
   * Wenn nichts angegeben ist, wird eine leere Lastschrift erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		Lastschrift u = null;

		if (context instanceof Lastschrift)
		{
			u = (Lastschrift) context;
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.LastschriftNew.class,u);
  }
}
