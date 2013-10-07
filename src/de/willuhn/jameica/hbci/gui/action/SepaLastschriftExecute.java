/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SepaLastschriftDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer SEPA-Lastschrift verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SepaLastschrift</code> als Context.
 */
public class SepaLastschriftExecute implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
	 * Erwartet ein Objekt vom Typ <code>SepaLastschrift</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof SepaLastschrift))
			throw new ApplicationException(i18n.tr("Kein Auftrag angegeben"));

		try
		{
			final SepaLastschrift u = (SepaLastschrift) context;
			
			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Lastschrift wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			SepaLastschriftDialog d = new SepaLastschriftDialog(u,SepaLastschriftDialog.POSITION_CENTER);
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

	    Konto konto = u.getKonto();
	    Class<SynchronizeJobSepaLastschrift> type = SynchronizeJobSepaLastschrift.class;

	    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
	    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
	    SynchronizeBackend backend = engine.getBackend(type,konto);
	    SynchronizeJob job         = backend.create(type,konto);
	    
	    job.setContext(SynchronizeJob.CTX_ENTITY,u);
	    
	    backend.execute(Arrays.asList(job));
		}
		catch (RemoteException e)
		{
			Logger.error("error while executing transfer",e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen des Auftrages"),StatusBarMessage.TYPE_ERROR));
		}
  }

}
