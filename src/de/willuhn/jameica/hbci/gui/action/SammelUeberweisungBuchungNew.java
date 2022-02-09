/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
   */
  @Override
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
