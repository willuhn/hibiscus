/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastschriftNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/28 16:28:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
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
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelLastschrift u = null;

		if (context instanceof SammelLastschrift)
		{
			u = (SammelLastschrift) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastschriftNew.class,u);
  }

}


/**********************************************************************
 * $Log: SammelLastschriftNew.java,v $
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/