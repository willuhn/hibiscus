/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/TurnusDelete.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/11/13 17:12:15 $
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
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Empfaengers.
 */
public class TurnusDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Turnus</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Turnus))
			throw new ApplicationException(i18n.tr("Kein Turnus ausgewählt"));

		try {

			Turnus t = (Turnus) context;
			if (t.isNewObject())
				return;

			if (t.isInitial())
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Turnus kann nicht gelöscht werden, da er Bestandteil der Initialdaten ist"));
				return;
			}

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Turnus löschen"));
			d.setText(i18n.tr("Wollen Sie diesen Zahlungsturnus wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting turnus",e);
				return;
			}

			// ok, wir loeschen das Objekt
			t.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Zahlungsturnus gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Zahlungsturnus."));
			Logger.error("unable to delete turnus",e);
		}
  }

}


/**********************************************************************
 * $Log: TurnusDelete.java,v $
 * Revision 1.2  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/