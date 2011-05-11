/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungExecute.java,v $
 * $Revision: 1.2 $
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
import de.willuhn.jameica.hbci.gui.dialogs.AuslandsUeberweisungDialog;
import de.willuhn.jameica.hbci.gui.views.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.server.hbci.HBCIAuslandsUeberweisungJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Auslandsueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
 */
public class AuslandsUeberweisungExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null || !(context instanceof AuslandsUeberweisung))
			throw new ApplicationException(i18n.tr("Kein Auftrag angegeben"));

		try
		{
			final AuslandsUeberweisung u = (AuslandsUeberweisung) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			AuslandsUeberweisungDialog d = new AuslandsUeberweisungDialog(u,AuslandsUeberweisungDialog.POSITION_CENTER);
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
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Auftrages"));
				return;
			}

      // Wir merken uns die aktuelle Seite und aktualisieren sie nur,
      // wenn sie sich nicht geaendert hat.
      final AbstractView oldView = GUI.getCurrentView();

      HBCIFactory factory = HBCIFactory.getInstance();
      factory.addJob(new HBCIAuslandsUeberweisungJob(u));
      factory.executeJobs(u.getKonto(), new Listener() {
        public void handleEvent(Event event)
        {
          final AbstractView newView = GUI.getCurrentView();
          if (oldView == newView && u == newView.getCurrentObject())
            GUI.startView(AuslandsUeberweisungNew.class,u);
        }
      }); 

		}
		catch (RemoteException e)
		{
			Logger.error("error while executing transfer",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Auftrages"));
		}
  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungExecute.java,v $
 * Revision 1.2  2011/05/11 10:05:32  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/