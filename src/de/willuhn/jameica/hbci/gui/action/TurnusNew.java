/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/TurnusNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/13 17:02:03 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer neuen Zahlungsturnus.
 */
public class TurnusNew implements Action
{

  /**
   * Als Context wird ein Turnus-Objekt erwartet.
   * Wenn nichts angegeben ist, wird ein leerer Turnus erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		Turnus t = null;

		if (context != null && (context instanceof Turnus))
		{
			t = (Turnus) context;
		}
		else
		{
			try {
				t = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
			}
			catch (RemoteException e)
			{
				throw new ApplicationException(i18n.tr("Fehler beim Anlegen des Zahlungsturnus"));
			}
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.TurnusNeu.class.getName(),t);
  }

}


/**********************************************************************
 * $Log: TurnusNew.java,v $
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/