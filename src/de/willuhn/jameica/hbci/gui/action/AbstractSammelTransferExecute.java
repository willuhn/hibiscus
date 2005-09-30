/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AbstractSammelTransferExecute.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
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
import de.willuhn.jameica.hbci.gui.dialogs.SammelTransferDialog;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung eines Sammel-Auftrages verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
 */
public abstract class AbstractSammelTransferExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof SammelTransfer))
			throw new ApplicationException(i18n.tr("Kein Sammel-Auftrag angegeben"));

		try
		{
			final SammelTransfer u = (SammelTransfer) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Sammel-Auftrag wurde bereits ausgeführt"));

			if (u.getBuchungen().size() == 0)
				throw new ApplicationException(i18n.tr("Sammel-Auftrag enthält keine Buchungen"));
			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			SammelTransferDialog d = new SammelTransferDialog(u,SammelTransferDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Sammel-Auftrages"));
				return;
			}
			execute(u);
		}
		catch (RemoteException e)
		{
			Logger.error("error while executing sammelauftrag",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Sammel-Auftrages"));
		}
  }

  /**
   * Wird aufgerufen, nachdem alle Sicherheitsabfragen erfolgt sind.
   * Hier muss die implementierende Klasse den Auftrag ausfuehren und dann zur
   * gewuenschten Zielseite wechseln.
   * @param transfer
   * @throws ApplicationException
   * @throws RemoteException
   */
  abstract void execute(SammelTransfer transfer) throws ApplicationException, RemoteException;
}


/**********************************************************************
 * $Log: AbstractSammelTransferExecute.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/