/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SammelLastschriftExecute.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/07/26 23:57:18 $
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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SammelLastschriftDialog;
import de.willuhn.jameica.hbci.gui.views.SammelLastschriftNew;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelLastschriftJob;
import de.willuhn.jameica.system.Application;
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

		if (context == null || !(context instanceof SammelLastschrift))
			throw new ApplicationException(i18n.tr("Keine Sammel-Lastschrift angegeben"));

		try
		{
			final SammelLastschrift u = (SammelLastschrift) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Sammel-Lastschrift wurde bereits ausgeführt"));

			if (u.getBuchungen().size() == 0)
				throw new ApplicationException(i18n.tr("Sammel-Lastschrift enthält keine Buchungen"));
			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			SammelLastschriftDialog d = new SammelLastschriftDialog(u,SammelLastschriftDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)d.open()).booleanValue())
					return;
			}
			catch (Exception e)
			{
				Logger.error("error while showing confirm dialog",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen der Sammel-Lastschrift"));
				return;
			}

      HBCIFactory factory = HBCIFactory.getInstance();
      factory.addJob(new HBCISammelLastschriftJob(u));
      factory.executeJobs(u.getKonto(), new Listener() {
        public void handleEvent(Event event)
        {
          // BUGZILLA 31 http://www.willuhn.de/bugzilla/show_bug.cgi?id=31
          GUI.startView(SammelLastschriftNew.class,u);
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
 * Revision 1.7  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.6  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.5  2005/03/30 23:28:13  web0
 * @B bug 31
 *
 * Revision 1.4  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.3  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/