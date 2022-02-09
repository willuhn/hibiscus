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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Sammel-Lastschrift.
 */
public class SammelLastschriftNew implements Action
{

  /**
   * Als Context kann ein Konto oder eine Sammel-Lastschrift angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Lastschrift
   * vorausgefuellt oder die uebergebene Lastschrift geladen.
   * Wenn nichts angegeben ist, wird eine leere Sammel-Lastschrift erstellt und angezeigt.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		SammelLastschrift u = null;

		if (context instanceof SammelLastschrift)
		{
			u = (SammelLastschrift) context;
		}

		GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastschriftNew.class,u);
  }

}
