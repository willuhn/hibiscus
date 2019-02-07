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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Kontos.
 */
public class KontoDelete implements Action
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
			d.setTitle(i18n.tr("Konto löschen"));
			d.setText(i18n.tr("Wollen Sie dieses Konto wirklich löschen?\nHierbei werden auch alle Umsätze und Aufträge des Kontos lokal gelöscht."));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
	    catch (OperationCanceledException oce)
	    {
	      Logger.info(oce.getMessage());
	      return;
	    }
			catch (Exception e)
			{
				Logger.error("error while deleting konto",e);
				return;
			}

			// ok, wir loeschen das Objekt
			k.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Konto gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Kontos."));
			Logger.error("unable to delete konto",e);
		}
  }

}
