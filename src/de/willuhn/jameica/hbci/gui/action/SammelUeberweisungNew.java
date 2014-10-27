/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungNew.java,v $
 * $Revision: 1.3 $
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
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Sammel-Ueberweisung.
 */
public class SammelUeberweisungNew implements Action
{

  /**
   * Als Context kann ein Konto oder eine Sammel-Ueberweisung angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt oder die uebergebene Ueberweisung geladen.
   * Wenn nichts angegeben ist, wird eine leere Sammel-Ueberweisung erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelUeberweisung u = null;

		if (context instanceof SammelUeberweisung)
		{
			u = (SammelUeberweisung) context;
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelUeberweisungNew.class,u);
  }

}
