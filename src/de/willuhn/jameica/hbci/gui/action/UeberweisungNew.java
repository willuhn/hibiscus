/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungNew.java,v $
 * $Revision: 1.8 $
 * $Date: 2007/02/21 11:58:52 $
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
import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.ClipboardUeberweisungImporter;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Ueberweisung.
 */
public class UeberweisungNew implements Action
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
        Logger.error("error while creating transfer",e);
				// Dann halt nicht
			}
		}
		else if (context instanceof Adresse)
		{
			try {
				Adresse e = (Adresse) context;
				u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
				u.setGegenkonto(e);
			}
			catch (RemoteException e)
			{
        Logger.error("error while creating transfer",e);
				// Dann halt nicht
			}
		}
    else if (context instanceof Umsatz)
    {
      // BUGZILLA 315
      try
      {
        Umsatz umsatz = (Umsatz) context;
        u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
        u.setBetrag(Math.abs(umsatz.getBetrag()));
        u.setGegenkontoBLZ(umsatz.getEmpfaengerBLZ());
        u.setGegenkontoName(umsatz.getEmpfaengerName());
        u.setGegenkontoNummer(umsatz.getEmpfaengerKonto());
        u.setKonto(umsatz.getKonto());
        u.setTermin(new Date());
        u.setZweck(umsatz.getZweck());
        u.setZweck2(umsatz.getZweck2());
      }
      catch (RemoteException re)
      {
        Logger.error("error while creating transfer",re);
        // Dann halt nicht
      }
      
    }
		else 
		{
      ClipboardUeberweisungImporter i = new ClipboardUeberweisungImporter();
      u = i.getUeberweisung();
    }
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UeberweisungNew.class,u);
 	}
}


/**********************************************************************
 * $Log: UeberweisungNew.java,v $
 * Revision 1.8  2007/02/21 11:58:52  willuhn
 * @N Bug 315
 *
 * Revision 1.7  2006/10/23 21:16:51  willuhn
 * @N eBaykontoParser umbenannt und ueberarbeitet
 *
 * Revision 1.6  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.2  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/