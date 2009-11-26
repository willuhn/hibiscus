/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftNew.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/11/26 12:00:21 $
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
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.logging.Logger;
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
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
        if ((k.getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED)
  				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Address)
		{
			try {
				Address e = (Address) context;
				u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
				u.setGegenkonto(e);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
    else if (context instanceof SammelLastBuchung)
    {
      try
      {
        SammelLastBuchung b = (SammelLastBuchung) context;
        SammelTransfer st = b.getSammelTransfer();
        u = (Lastschrift) Settings.getDBService().createObject(Lastschrift.class,null);
        u.setBetrag(b.getBetrag());
        u.setGegenkontoBLZ(b.getGegenkontoBLZ());
        u.setGegenkontoName(b.getGegenkontoName());
        u.setGegenkontoNummer(b.getGegenkontoNummer());
        u.setZweck(b.getZweck());
        u.setZweck2(b.getZweck2());
        u.setWeitereVerwendungszwecke(b.getWeitereVerwendungszwecke());
        if (st != null)
        {
          u.setKonto(st.getKonto());
          u.setTermin(st.getTermin());
        }
      }
      catch (RemoteException re)
      {
        Logger.error("error while creating transfer",re);
        // Dann halt nicht
      }
    }

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.LastschriftNew.class,u);
  }

}


/**********************************************************************
 * $Log: LastschriftNew.java,v $
 * Revision 1.6  2009/11/26 12:00:21  willuhn
 * @N Buchungen aus Sammelauftraegen in Einzelauftraege duplizieren
 *
 * Revision 1.5  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.4  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.3  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/