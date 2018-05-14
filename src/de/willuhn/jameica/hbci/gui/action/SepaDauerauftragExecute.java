/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Arrays;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SepaDauerauftragDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragStore;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zur Ausfuehrung eines neu angelegten SEPA-Dauerauftrag.
 */
public class SepaDauerauftragExecute implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * Erwartet einen SepaDauerauftrag als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (! (context instanceof SepaDauerauftrag))
			throw new ApplicationException(i18n.tr("Kein SEPA-Dauerauftrag angegeben"));

		try
		{
			final SepaDauerauftrag d = (SepaDauerauftrag) context;
			
			SepaDauerauftragDialog dd = new SepaDauerauftragDialog(d,SepaDauerauftragDialog.POSITION_CENTER);
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
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen des SEPA-Dauerauftrages"),StatusBarMessage.TYPE_ERROR));
				return;
			}

      Konto konto = d.getKonto();
      Class<SynchronizeJobSepaDauerauftragStore> type = SynchronizeJobSepaDauerauftragStore.class;

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
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des SEPA-Dauerauftrag"));
		}
  }
}
