/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastBuchungNew.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/03/05 19:11:25 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer neue Buchung in einer Sammel-Lastschrift.
 */
public class SammelLastBuchungNew implements Action
{

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Als Context kann eine Sammel-Lastschrift oder eine einzelne
   * Buchung einer Lastschrift angegeben werden. Abhaengig davon wird
   * entweder eine neue Buchung erzeugt oder die existierende
   * geoeffnet.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		SammelLastBuchung u = null;

		if (context instanceof SammelLastBuchung)
		{
			u = (SammelLastBuchung) context;
		}
		else
		{
			try {
        SammelLastschrift s = (SammelLastschrift) context;
				u = (SammelLastBuchung) Settings.getDBService().createObject(SammelLastBuchung.class,null);
				if (s.isNewObject())
					throw new ApplicationException(i18n.tr("Bitte speichern Sie zunächst die Sammel-Lastschrift"));
				u.setSammelLastschrift(s);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SammelLastBuchungNew.class,u);
  }

}


/**********************************************************************
 * $Log: SammelLastBuchungNew.java,v $
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