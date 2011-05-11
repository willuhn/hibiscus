/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/SammelTransferBuchungDelete.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:20:28 $
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
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen einer Buchung in eines Sammel-Auftrages.
 */
public class SammelTransferBuchungDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>SammelTransferBuchung</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelTransferBuchung))
			throw new ApplicationException(i18n.tr("Keine Buchung ausgewählt"));

		try {

			SammelTransferBuchung u = (SammelTransferBuchung) context;
			if (u.isNewObject())
				return;

			if (u.getSammelTransfer().ausgefuehrt())
				throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt" +					"und kann daher nur noch als ganzes gelöscht werden"));

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Buchung löschen"));
			d.setText(i18n.tr("Wollen Sie diese Buchung wirklich löschen?"));

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
				Logger.error("error while deleting buchung",e);
				return;
			}

			// ok, wir loeschen das Objekt
			u.delete();
			GUI.getStatusBar().setSuccessText(i18n.tr("Buchung gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen der Buchung."));
			Logger.error("unable to delete buchung",e);
		}
  }

}

/**********************************************************************
 * $Log: SammelTransferBuchungDelete.java,v $
 * Revision 1.2  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/