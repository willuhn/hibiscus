/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchDauerauftraege.java,v $
 * $Revision: 1.7 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die die Dauerauftraege eines Kontos abruft.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchDauerauftraege implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
	 * Fehlt das Konto, dann wird es in einem Dialog abgefragt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
		{
			// 1) Wir zeigen einen Dialog an, in dem der User das Konto auswaehlt
			KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
			try
			{
				context = d.open();
			}
			catch (OperationCanceledException oce)
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Vorgang abgebrochen"));
				return;
			}
			catch (Exception e)
			{
				Logger.error("error while choosing konto",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Kontos."));
			}
		}

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

		final Konto k = (Konto) context;
		GUI.startSync(new Runnable() {
			public void run() {
				try {
					GUI.getStatusBar().setStatusText(i18n.tr("Daueraufträge werden abgerufen..."));
					GUI.getStatusBar().startProgress();
					HBCIFactory factory = HBCIFactory.getInstance();
					factory.addJob(new HBCIDauerauftragListJob(k));
					factory.executeJobs(k.getPassport().getHandle());
					GUI.getStatusBar().setSuccessText(i18n.tr("...Daueraufträge erfolgreich übertragen"));

					new DauerauftragListe().handleAction(k);
				}
				catch (OperationCanceledException oce)
				{
					GUI.getStatusBar().setErrorText(i18n.tr("Vorgang abgebrochen"));
				}
				catch (ApplicationException e2)
				{
					GUI.getView().setErrorText(i18n.tr(e2.getMessage()));
				}
				catch (RemoteException e)
				{
					Logger.error("error while reading dauerauftraege",e);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen der Daueraufträge.") + " [" + e.getMessage() + "]");
				}
				finally
				{
					GUI.getStatusBar().stopProgress();
					GUI.getStatusBar().setStatusText("");
				}
			}
		});
  }

}


/**********************************************************************
 * $Log: KontoFetchDauerauftraege.java,v $
 * Revision 1.7  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/23 18:13:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.1  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.3  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/