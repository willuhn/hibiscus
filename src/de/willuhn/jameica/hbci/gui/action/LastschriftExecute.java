/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftExecute.java,v $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.LastschriftDialog;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCILastschriftJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Lastschrift verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>Lastschrift</code> als Context.
 */
public class LastschriftExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Lastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Lastschrift))
			throw new ApplicationException(i18n.tr("Keine Lastschrift angegeben"));

		try
		{
			final Lastschrift u = (Lastschrift) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Lastschrift wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			LastschriftDialog d = new LastschriftDialog(u,LastschriftDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Lastschrift"));
				return;
			}

			GUI.startSync(new Runnable()
      {
        public void run()
        {
        	try
        	{
						GUI.getStatusBar().startProgress();
						GUI.getStatusBar().setStatusText(i18n.tr("Führe Lastschrift aus..."));
						HBCIFactory factory = HBCIFactory.getInstance();
						factory.addJob(new HBCILastschriftJob(u));
						factory.executeJobs(u.getKonto().getPassport().getHandle()); 
						GUI.getStatusBar().setSuccessText(i18n.tr("Lastschrift erfolgreich ausgeführt"));
        	}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausführung der Lastschrift abgebrochen"));
					}
					catch (ApplicationException ae)
					{
						GUI.getStatusBar().setErrorText(ae.getMessage());
					}
					catch (RemoteException e)
					{
						Logger.error("error while executing ueberweisung",e);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Lastschrift") + " [" + e.getMessage() + "]");
					}
					finally
					{
						GUI.getStatusBar().stopProgress();
						GUI.getStatusBar().setStatusText("");
					}
        }
      });

		}
		catch (RemoteException e)
		{
			Logger.error("error while executing lastschrift",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Lastschrift"));
		}
  }

}


/**********************************************************************
 * $Log: LastschriftExecute.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/