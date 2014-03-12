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
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Buchung in einer SEPA-Sammel-Lastschrift.
 */
public class SepaSammelLastBuchungNew implements Action
{

  /**
   * Als Context kann eine SEPA-Sammel-Lastschrift oder eine einzelne
   * Buchung einer Lastschrift angegeben werden. Abhaengig davon wird
   * entweder eine neue Buchung erzeugt oder die existierende
   * geoeffnet.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    SepaSammelLastBuchung u = null;

		if (context instanceof SepaSammelLastBuchung)
		{
			u = (SepaSammelLastBuchung) context;
		}
		else
		{
			try {
        SepaSammelLastschrift s = (SepaSammelLastschrift) context;
        u = (SepaSammelLastBuchung) s.createBuchung();
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaSammelLastBuchungNew.class,u);
  }

}
