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
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Buchung in einer Sammel-Lastschrift.
 */
public class SammelLastBuchungNew implements Action
{

  /**
   * Als Context kann eine Sammel-Lastschrift oder eine einzelne
   * Buchung einer Lastschrift angegeben werden. Abhaengig davon wird
   * entweder eine neue Buchung erzeugt oder die existierende
   * geoeffnet.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelLastBuchung u = null;

		if (context instanceof SammelLastBuchung)
		{
			u = (SammelLastBuchung) context;
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastBuchungNew.class,u);
  }

}
