/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/LastschriftDelete.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/19 00:16:04 $
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
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen einer Lastschrift.
 */
public class LastschriftDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Lastschrift</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Lastschrift))
			throw new ApplicationException(i18n.tr("Keine Lastschrift ausgewählt"));

		try {

			Lastschrift u = (Lastschrift) context;
			if (u.isNewObject())
				return;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Lastschrift löschen"));
			d.setText(i18n.tr("Wollen Sie diese Lastschrift wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting lastschrift",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Lastschrift gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Lastschrift."));
			Logger.error("unable to delete lastschrift",e);
		}
  }

}


/**********************************************************************
 * $Log: LastschriftDelete.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/