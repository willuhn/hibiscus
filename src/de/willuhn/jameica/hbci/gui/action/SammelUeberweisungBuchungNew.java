/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungBuchungNew.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/08/07 14:31:59 $
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
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Buchung in einer Sammel-Ueberweisung.
 */
public class SammelUeberweisungBuchungNew implements Action
{

  /**
   * Als Context kann eine Sammel-Ueberweisung oder eine einzelne
   * Buchung einer Sammel-Ueberweisung angegeben werden. Abhaengig davon wird
   * entweder eine neue Buchung erzeugt oder die existierende
   * geoeffnet.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelUeberweisungBuchung u = null;

		if (context instanceof SammelUeberweisungBuchung)
		{
			u = (SammelUeberweisungBuchung) context;
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelUeberweisungBuchungNew.class,u);
  }

}
