/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragDelete.java,v $
 * $Revision: 1.17 $
 * $Date: 2007/02/21 10:02:27 $
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
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.DauerauftragDeleteDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragDeleteJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

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

        DauerauftragDeleteDialog d2 = new DauerauftragDeleteDialog(DauerauftragDeleteDialog.POSITION_CENTER);
				Date fd = null;
				try
				{
					fd = (Date) d2.open();
				}
        catch (OperationCanceledException ce)
        {
          return;
        }
				catch (Exception e)
				{
					// OK, dann halt ohne Datum
					fd = null;
				}
				final Date date = fd;

				// Uh, der wird auch online geloescht
        // BUGZILLA #15 http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
        HBCIFactory factory = HBCIFactory.getInstance();
        HBCIDauerauftragListJob job = new HBCIDauerauftragListJob(da.getKonto());
        job.setExclusive(true);
        factory.addJob(job);
        factory.addJob(new HBCIDauerauftragDeleteJob(da,date));
        factory.executeJobs(da.getKonto(), new Listener() {
          public void handleEvent(Event event)
          {
            if (event.type == ProgressMonitor.STATUS_DONE)
            {
              try
              {
                da.delete();
              }
              catch (ApplicationException e)
              {
                GUI.getStatusBar().setErrorText(e.getMessage());
              }
              catch (RemoteException re)
              {
                Logger.error("unable to delete local da copy",re);
                GUI.getStatusBar().setErrorText(i18n.tr("Lokale Kopie des Dauerauftrages konnte nicht gelöscht werden"));
              }
              
            }
          }
        }); 
			}
			else
			{
				// nur lokal loeschen
				da.delete();
				GUI.getStatusBar().setSuccessText(i18n.tr("Dauerauftrag gelöscht."));
			}
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
 * Revision 1.17  2007/02/21 10:02:27  willuhn
 * @C Code zum Ausfuehren exklusiver Jobs redesigned
 *
 * Revision 1.16  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.15  2005/06/23 17:07:38  web0
 * @R removed debug code
 *
 * Revision 1.14  2005/06/23 17:05:33  web0
 * @B bug 85
 *
 * Revision 1.13  2005/06/07 21:57:25  web0
 * @B bug 18
 *
 * Revision 1.12  2005/05/10 22:26:15  web0
 * @B bug 71
 *
 * Revision 1.11  2005/03/04 00:16:43  web0
 * @B Bugzilla http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 *
 * Revision 1.10  2005/02/28 23:59:57  web0
 * @B http://www.willuhn.de/bugzilla/show_bug.cgi?id=15
 *
 * Revision 1.9  2005/02/28 15:30:47  web0
 * @B Bugzilla #15
 *
 * Revision 1.8  2004/11/17 19:02:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
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
 * Revision 1.2  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.1  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 **********************************************************************/