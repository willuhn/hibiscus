/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelUeberweisungNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue Sammel-Ueberweisung.
 */
public class SammelUeberweisungNew implements Action
{

  /**
   * Als Context kann ein Konto oder eine Sammel-Ueberweisung angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt oder die uebergebene Ueberweisung geladen.
   * Wenn nichts angegeben ist, wird eine leere Sammel-Ueberweisung erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelUeberweisung u = null;

		if (context instanceof SammelUeberweisung)
		{
			u = (SammelUeberweisung) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (SammelUeberweisung) Settings.getDBService().createObject(SammelUeberweisung.class,null);
				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

		try
    {
      if (u == null)
        u = (SammelUeberweisung) Settings.getDBService().createObject(SammelUeberweisung.class,null);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to create sammelueberweisung",e);
    }

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelUeberweisungNew.class,u);
  }

}


/**********************************************************************
 * $Log: SammelUeberweisungNew.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.2  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/