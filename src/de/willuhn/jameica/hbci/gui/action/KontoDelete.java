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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoDeleteDialog;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

		try {

			Konto k = (Konto) context;
			if (k.isNewObject())
				return;

			KontoDeleteDialog d = new KontoDeleteDialog(k);
			
			try {
				Boolean choice = (Boolean) d.open();
				if (choice == null || !choice.booleanValue())
					return;
			}
	    catch (OperationCanceledException oce)
	    {
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
