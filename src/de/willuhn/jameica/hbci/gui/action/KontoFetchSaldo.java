/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchSaldo.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/23 18:13:45 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action, die den Saldo eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchSaldo implements Action
{

	private I18N i18n = null;

  /**
   * ct.
   */
  public KontoFetchSaldo()
  {
    super();
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

		try {
			final Konto k = (Konto) context;
			if (k == null)
				return;

			if (k.isNewObject())
			{
				GUI.getView().setErrorText(i18n.tr("Bitte speichern Sie zuerst das Konto."));
				return;
			}

			GUI.getStatusBar().startProgress();

			GUI.startSync(new Runnable() {
				public void run() {
					try {
						k.refreshSaldo();
						new de.willuhn.jameica.hbci.gui.action.KontoNeu().handleAction(k);
					}
					catch (ApplicationException e2)
					{
						GUI.getStatusBar().setErrorText(e2.getMessage());
					}
					catch (Throwable t)
					{
						Logger.error("error while reading saldo",t);
						GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen des Saldos."));
					}
				}
			});
		}
		catch (RemoteException e)
		{
			Logger.error("error while refreshing saldo",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Aktualisieren des Saldos"));
		}
		finally
		{
			GUI.getStatusBar().stopProgress();
		}
  }

}


/**********************************************************************
 * $Log: KontoFetchSaldo.java,v $
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