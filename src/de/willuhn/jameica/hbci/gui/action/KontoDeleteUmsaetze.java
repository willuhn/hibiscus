/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/KontoDeleteUmsaetze.java,v $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer Loeschen der Umsaetze eines Kontos.
 */
public class KontoDeleteUmsaetze implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

		try {

			Konto k = (Konto) context;
			if (k.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Umsätze löschen"));
			d.setText(i18n.tr("Sind Sie sicher, daß Sie alle Umsätze des Kontos löschen möchten?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting umsaetze",e);
				return;
			}

			// ok, wir loeschen das Objekt
			k.deleteUmsaetze();
			GUI.getStatusBar().setSuccessText(i18n.tr("Umsätze gelöscht."));
			GUI.startPreviousView();
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Umsätze."));
			Logger.error("unable to delete umsaetze",e);
		}
  }

}


/**********************************************************************
 * $Log: KontoDeleteUmsaetze.java,v $
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 **********************************************************************/