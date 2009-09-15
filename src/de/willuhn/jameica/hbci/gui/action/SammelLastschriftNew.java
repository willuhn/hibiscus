/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastschriftNew.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/09/15 00:23:34 $
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
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.logging.Logger;
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
        if ((k.getFlags() & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED)
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
        u = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to create sammellastschrift",e);
    }

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastschriftNew.class,u);
  }

}


/**********************************************************************
 * $Log: SammelLastschriftNew.java,v $
 * Revision 1.3  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.2  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/