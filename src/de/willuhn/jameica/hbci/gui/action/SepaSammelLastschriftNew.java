/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue SEPA-Sammel-Lastschrift.
 */
public class SepaSammelLastschriftNew implements Action
{

  /**
   * Als Context kann ein Konto oder eine SEPA-Sammel-Lastschrift angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Lastschrift
   * vorausgefuellt oder die uebergebene Lastschrift geladen.
   * Wenn nichts angegeben ist, wird eine leere Sammel-Lastschrift erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SepaSammelLastschrift u = null;

		if (context instanceof SepaSammelLastschrift)
		{
			u = (SepaSammelLastschrift) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (SepaSammelLastschrift) Settings.getDBService().createObject(SepaSammelLastschrift.class,null);
				if (!k.hasFlag(Konto.FLAG_DISABLED) && !k.hasFlag(Konto.FLAG_OFFLINE) && StringUtils.trimToNull(k.getIban()) != null)
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
        u = (SepaSammelLastschrift) Settings.getDBService().createObject(SepaSammelLastschrift.class,null);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to create SEPA sammellastschrift",e);
    }

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaSammelLastschriftNew.class,u);
  }

}
