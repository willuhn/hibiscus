/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoSyncViaScripting.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/07/25 23:11:59 $
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
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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

		try {
			final Konto k = (Konto) context;
	    if ((k.getFlags() & Konto.FLAG_OFFLINE) != Konto.FLAG_OFFLINE)
	      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Offline-Konto aus"));

			if (k.isNewObject())
				k.store();
			
      final AbstractView currentView = GUI.getCurrentView();
			Application.getMessagingFactory().getMessagingQueue("jameica.scripting").sendSyncMessage(new QueryMessage("hibiscus_kontoSync",k));
			
			// GUI neu laden
      AbstractView newView = GUI.getCurrentView();
      if (newView == currentView)
        currentView.reload();
		}
		catch (RemoteException e)
		{
			Logger.error("error while syncing konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren des Kontos"));
		}
  }
}


/**********************************************************************
 * $Log: KontoSyncViaScripting.java,v $
 * Revision 1.1  2010/07/25 23:11:59  willuhn
 * @N Erster Code fuer Scripting-Integration
 *
 **********************************************************************/