/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragDelete.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/11/12 18:25:07 $
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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer Loeschen eines Dauerauftrages.
 * Existiert der Auftrag auch bei der Bank, wird er dort ebenfalls geloescht.
 */
public class DauerauftragDelete implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Dauerauftrag</code> im Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Kein Dauerauftrag ausgewählt"));

		try {

			final Dauerauftrag da = (Dauerauftrag) context;

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(i18n.tr("Dauerauftrag löschen"));
			if (da.isActive())
				d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich löschen?\nDer Auftrag wird auch bei der Bank gelöscht."));
			else
				d.setText(i18n.tr("Wollen Sie diesen Dauerauftrag wirklich löschen?"));

			try {
				Boolean choice = (Boolean) d.open();
				if (!choice.booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while deleting dauerauftrag",e);
				return;
			}

			if (da.isActive())
			{

				// Uh, der wird auch online geloescht
				GUI.startSync(new Runnable()
				{
					public void run()
					{
						try
						{
							GUI.getStatusBar().startProgress();
							GUI.getStatusBar().setStatusText(i18n.tr("Lösche Dauerauftrag bei Bank..."));
							HBCIFactory factory = HBCIFactory.getInstance();
							factory.addJob(new HBCIDauerauftragDeleteJob(da));
							factory.executeJobs(da.getKonto().getPassport().getHandle()); 
							GUI.getStatusBar().setSuccessText(i18n.tr("...Dauerauftrag erfolgreich gelöscht"));
						}
						catch (OperationCanceledException oce)
						{
							GUI.getStatusBar().setErrorText(i18n.tr("Vorgang abgebrochen"));
						}
						catch (ApplicationException ae)
						{
							GUI.getStatusBar().setErrorText(ae.getMessage());
						}
						catch (RemoteException e)
						{
							Logger.error("error while deleting dauerauftrag",e);
							GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Dauerauftrages") + " [" + e.getMessage() + "]");
						}
						finally
						{
							GUI.getStatusBar().stopProgress();
							GUI.getStatusBar().setStatusText("");
						}
					}
				});
			}
			else
			{
				// nur lokal loeschen
				da.delete();
			}
			GUI.getStatusBar().setSuccessText(i18n.tr("Dauerauftrag gelöscht."));
		}
		catch (RemoteException e)
		{
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Dauerauftrages."));
			Logger.error("unable to delete dauerauftrag",e);
		}
  }

}


/**********************************************************************
 * $Log: DauerauftragDelete.java,v $
 * Revision 1.5  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 **********************************************************************/