/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Buchung in einer SEPA-Sammel-Ueberweisung.
 */
public class SepaSammelUeberweisungBuchungNew implements Action
{

  /**
   * Als Context kann eine SEPA-Sammel-Ueberweisung oder eine einzelne
   * Buchung einer Ueberweisung angegeben werden. Abhaengig davon wird
   * entweder eine neue Buchung erzeugt oder die existierende
   * geoeffnet.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    SepaSammelUeberweisungBuchung u = null;

		if (context instanceof SepaSammelUeberweisungBuchung)
		{
			u = (SepaSammelUeberweisungBuchung) context;
		}
		else
		{
			try {
        SepaSammelUeberweisung s = (SepaSammelUeberweisung) context;
        u = (SepaSammelUeberweisungBuchung) s.createBuchung();
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaSammelUeberweisungBuchungNew.class,u);
  }

}
