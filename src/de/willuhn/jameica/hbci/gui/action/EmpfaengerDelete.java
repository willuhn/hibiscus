/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/EmpfaengerDelete.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/20 12:08:18 $
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
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer Loeschen eines Empfaengers.
 */
public class EmpfaengerDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Empfaenger</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Empfaenger))
			throw new ApplicationException(i18n.tr("Keine Empfängeradresse ausgewählt"));

		try {

			Empfaenger empf = (Empfaenger) context;
			if (empf.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Empfängeradresse löschen"));
			d.setText(i18n.tr("Wollen Sie diese Empfängeradresse wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting empfaenger",e);
				return;
			}

			// ok, wir loeschen das Objekt
			empf.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Empfängeradresse gelöscht."));
			GUI.startPreviousView();
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Empfängeradresse."));
			Logger.error("unable to delete empfaenger",e);
		}
  }

}


/**********************************************************************
 * $Log: EmpfaengerDelete.java,v $
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/