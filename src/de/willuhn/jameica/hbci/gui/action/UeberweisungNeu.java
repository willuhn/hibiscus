/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UeberweisungNeu.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/18 23:38:17 $
 * $Author: willuhn $
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
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Ueberweisung.
 */
public class UeberweisungNeu implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder eine Ueberweisung angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt oder die uebergebene Ueberweisung geladen.
   * Wenn nichts angegeben ist, wird eine leere Ueberweisung erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		Ueberweisung u = null;

		if (context instanceof Ueberweisung)
		{
			u = (Ueberweisung) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Empfaenger)
		{
			try {
				Empfaenger e = (Empfaenger) context;
				u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
				u.setEmpfaenger(e);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.UeberweisungNeu.class.getName(),u);
  }

}


/**********************************************************************
 * $Log: UeberweisungNeu.java,v $
 * Revision 1.2  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/