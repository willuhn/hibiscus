/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastschriftExecute.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/02 00:22:05 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Sammel-Lastschrift verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelLastschrift</code> als Context.
 */
public class SammelLastschriftExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>SammelLastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Lastschrift))
			throw new ApplicationException(i18n.tr("Keine Sammel-Lastschrift angegeben"));

		try
		{
			final SammelLastschrift u = (SammelLastschrift) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Sammel-Lastschrift wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			// TODO SammelLastschriftDialog d = new SammelLastschriftDialog(u,LastschriftDialog.POSITION_CENTER);
//			try
//			{
//				if (!((Boolean)d.open()).booleanValue())
//					return;
//			}
//			catch (Exception e)
//			{
//				Logger.error("error while showing confirm dialog",e);
//				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Sammel-Lastschrift"));
//				return;
//			}

			GUI.startSync(new Runnable()
      {
        public void run()
        {
        	try
        	{
						GUI.getStatusBar().startProgress();
						GUI.getStatusBar().setStatusText(i18n.tr("Führe Sammel-Lastschrift aus..."));
						HBCIFactory factory = HBCIFactory.getInstance();
						// TODO factory.addJob(new HBCISammelLastschriftJob(u));
						factory.executeJobs(u.getKonto().getPassport().getHandle()); 
						GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift erfolgreich ausgeführt"));
        	}
					catch (OperationCanceledException oce)
					{
						GUI.getStatusBar().setErrorText(i18n.tr("Ausführung der Sammel-Lastschrift abgebrochen"));
					}
					catch (ApplicationException ae)
					{
						GUI.getStatusBar().setErrorText(ae.getMessage());
					}
					catch (RemoteException e)
					{
						Logger.error("error while executing sammellastschrift",e);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Sammel-Lastschrift") + " [" + e.getMessage() + "]");
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
			Logger.error("error while executing sammellastschrift",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Sammel-Lastschrift"));
		}
  }

}


/**********************************************************************
 * $Log: SammelLastschriftExecute.java,v $
 * Revision 1.1  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/