/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/LastschriftExecute.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/05/11 10:05:32 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.LastschriftDialog;
import de.willuhn.jameica.hbci.gui.views.LastschriftNew;
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
			catch (OperationCanceledException oce)
			{
			  Logger.info(oce.getMessage());
			  return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Lastschrift"));
				return;
			}

      // Wir merken uns die aktuelle Seite und aktualisieren sie nur,
      // wenn sie sich nicht geaendert hat.
      final AbstractView oldView = GUI.getCurrentView();

      HBCIFactory factory = HBCIFactory.getInstance();
      factory.addJob(new HBCILastschriftJob(u));
      factory.executeJobs(u.getKonto(), new Listener() {
        public void handleEvent(Event event)
        {
          final AbstractView newView = GUI.getCurrentView();
          if (oldView == newView && u == newView.getCurrentObject())
            GUI.startView(LastschriftNew.class,u);
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
 * Revision 1.8  2011/05/11 10:05:32  willuhn
 * @N OCE fangen
 *
 * Revision 1.7  2007/07/04 09:16:24  willuhn
 * @B Aktuelle View nach Ausfuehrung eines HBCI-Jobs nur noch dann aktualisieren, wenn sie sich zwischenzeitlich nicht geaendert hat
 *
 * Revision 1.6  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.5  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.4  2005/03/30 23:28:13  web0
 * @B bug 31
 *
 * Revision 1.3  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.2  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/