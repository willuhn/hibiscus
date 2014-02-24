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


/**********************************************************************
 * $Log: SammelLastBuchungNew.java,v $
 * Revision 1.6  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.5  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.4  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.3  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.2  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 **********************************************************************/