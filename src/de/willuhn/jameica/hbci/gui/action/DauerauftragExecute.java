/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/DauerauftragExecute.java,v $
 * $Revision: 1.12 $
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
import java.util.Arrays;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.DauerauftragDialog;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragStore;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zur Ausfuehrung eines neu angelegten Dauerauftrag.
 */
public class DauerauftragExecute implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * Erwartet einen Dauerauftrag als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (! (context instanceof Dauerauftrag))
			throw new ApplicationException(i18n.tr("Kein Dauerauftrag angegeben"));

		try
		{
			final Dauerauftrag d = (Dauerauftrag) context;
			
			DauerauftragDialog dd = new DauerauftragDialog(d,DauerauftragDialog.POSITION_CENTER);
			try
			{
				if (!((Boolean)dd.open()).booleanValue())
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
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen des Dauerauftrages"),StatusBarMessage.TYPE_ERROR));
				return;
			}

      Konto konto = d.getKonto();
      Class type = SynchronizeJobDauerauftragStore.class;

      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
      SynchronizeBackend backend = engine.getBackend(type,konto);
      SynchronizeJob job         = backend.create(type,konto);
      
      job.setContext(SynchronizeJob.CTX_ENTITY,d);
      
      backend.execute(Arrays.asList(job));
		}
		catch (RemoteException e)
		{
			Logger.error("error while executing dauerauftrag",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Dauerauftrag"));
		}
  }
}
