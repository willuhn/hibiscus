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

import java.util.Arrays;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragList;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die die SEPA-Dauerauftraege eines Kontos abruft.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchSepaDauerauftraege implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
	 * Fehlt das Konto, dann wird es in einem Dialog abgefragt.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		if (!(context instanceof Konto))
		{
			// 1) Wir zeigen einen Dialog an, in dem der User das Konto auswaehlt
			KontoAuswahlDialog d = new KontoAuswahlDialog(null,KontoFilter.createForeign(SynchronizeJobSepaDauerauftragList.class),KontoAuswahlDialog.POSITION_CENTER);
			try
			{
				context = d.open();
			}
			catch (OperationCanceledException oce)
			{
			  Logger.info("operation cancelled");
				return;
			}
			catch (Exception e)
			{
				Logger.error("error while choosing konto",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Auswahl des Kontos"),StatusBarMessage.TYPE_ERROR));
			}
		}

		if (!(context instanceof Konto))
			throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));

		final Konto konto = (Konto) context;
    Class<SynchronizeJobSepaDauerauftragList> type = SynchronizeJobSepaDauerauftragList.class;

    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
    SynchronizeBackend backend = engine.getBackend(type,konto);
    SynchronizeJob job         = backend.create(type,konto);
    
    job.setContext(SynchronizeJob.CTX_ENTITY,konto);
    
    backend.execute(Arrays.asList(job));
  }

}
