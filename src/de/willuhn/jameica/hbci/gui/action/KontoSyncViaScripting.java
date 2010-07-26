/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoSyncViaScripting.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/07/26 09:11:41 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action, die ein Konto via Jameica-Scripting aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoSyncViaScripting implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!Application.getPluginLoader().isInstalled("de.willuhn.jameica.scripting.Plugin"))
      return;
    
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    final Konto k = (Konto) context;
		try {
	    if ((k.getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE)
	      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Offline-Konto aus"));

			if (k.isNewObject())
				k.store();
    }
    catch (RemoteException e)
    {
      Logger.error("error while syncing konto",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren des Kontos"));
    }
			
    final AbstractView currentView = GUI.getCurrentView();
      
    Application.getController().start(new BackgroundTask() {
      
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
        monitor.setStatusText(i18n.tr("Starte Synchronisierung"));
        
        QueryMessage msg = new QueryMessage("hibiscus_kontoSync",new Object[]{k,monitor});
        Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendSyncMessage(msg);

        // GUI neu laden
        AbstractView newView = GUI.getCurrentView();
        if (newView == currentView)
          currentView.reload();

        monitor.setPercentComplete(100);
        Object value = msg.getData();
        if (value instanceof Throwable)
        {
          Throwable t = (Throwable) value;
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(i18n.tr("Fehler: {0}",t.getMessage()));
        }
        else
        {
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(i18n.tr("Synchronisierung erfolgreich beendet"));
        }
      }
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return false;
      }
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
      }
    });
  }
}


/**********************************************************************
 * $Log: KontoSyncViaScripting.java,v $
 * Revision 1.2  2010/07/26 09:11:41  willuhn
 * @N Scripting-Anbindung jetzt via Background-Task und Rueckmeldung via ProgressMonitor
 *
 * Revision 1.1  2010-07-25 23:11:59  willuhn
 * @N Erster Code fuer Scripting-Integration
 *
 **********************************************************************/