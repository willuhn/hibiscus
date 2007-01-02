/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoFetchUmsaetze.java,v $
 * $Revision: 1.14 $
 * $Date: 2007/01/02 11:32:14 $
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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die die Umsaetze eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchUmsaetze implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

		try {
			final Konto k = (Konto) context;

			if (k.isNewObject())
				k.store();

      final AbstractView currentView = GUI.getCurrentView();
			HBCIFactory factory = HBCIFactory.getInstance();
			factory.addJob(new HBCIUmsatzJob(k));

			// BUGZILLA #3 http://www.willuhn.de/bugzilla/show_bug.cgi?id=3
			factory.addExclusiveJob(new HBCISaldoJob(k));

			factory.executeJobs(k, new Listener() {
        public void handleEvent(Event event)
        {
          try
          {
            currentView.reload();
          }
          catch (ApplicationException e)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
          }
        }
      });
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while refreshing umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Abrufen der Umsätze"));
		}
  }

}


/**********************************************************************
 * $Log: KontoFetchUmsaetze.java,v $
 * Revision 1.14  2007/01/02 11:32:14  willuhn
 * @B reload current view
 *
 * Revision 1.13  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.12  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.11  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.10  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.9  2005/03/02 18:48:21  web0
 * @B Bugzilla #3 Saldo wird beim Abrufen der Umsaetze jetzt als exklusiver Job ausgefuehrt
 *
 * Revision 1.8  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.7  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.6  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/24 17:19:02  willuhn
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