/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neue SEPA-Sammel-Ueberweisung.
 */
public class SepaSammelUeberweisungNew implements Action
{

  /**
   * Als Context kann ein Konto oder eine SEPA-Sammel-Ueberweisung angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt oder die uebergebene Ueberweisung geladen.
   * Wenn nichts angegeben ist, wird eine leere Sammel-Ueberweisung erstellt und angezeigt.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		SepaSammelUeberweisung u = null;

		if (context instanceof SepaSammelUeberweisung)
		{
			u = (SepaSammelUeberweisung) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (SepaSammelUeberweisung) Settings.getDBService().createObject(SepaSammelUeberweisung.class,null);
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
        u = (SepaSammelUeberweisung) Settings.getDBService().createObject(SepaSammelUeberweisung.class,null);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to create SEPA sammelueberweisung",e);
    }

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaSammelUeberweisungNew.class,u);
  }

}
