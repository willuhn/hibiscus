/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/TurnusNew.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/13 17:12:14 $
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
import de.willuhn.jameica.gui.dialogs.ViewDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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
		// Wir starten das Bearbeiten in einem extra Fenster
		de.willuhn.jameica.hbci.gui.views.TurnusNew tn = new de.willuhn.jameica.hbci.gui.views.TurnusNew();
		tn.setCurrentObject(t);
		ViewDialog d = new ViewDialog(tn,ViewDialog.POSITION_MOUSE);
		d.setTitle(i18n.tr("Zahlungsturnus bearbeiten"));
		try
		{
			d.open();
		}
		catch (Throwable t2)
		{
			Logger.error("error while opening turnus dialog",t2);
			throw new ApplicationException(i18n.tr("Fehler beim Öffnen des Zahlungsturnus"),t2);
		}
  }

}


/**********************************************************************
 * $Log: TurnusNew.java,v $
 * Revision 1.2  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/