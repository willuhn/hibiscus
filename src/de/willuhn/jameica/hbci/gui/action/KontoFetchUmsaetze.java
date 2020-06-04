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
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die die Umsaetze eines Kontos aktualisiert.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchUmsaetze implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    
    Konto konto = (context instanceof Konto) ? (Konto) context : null;

		try
		{
	    final Class<SynchronizeJobKontoauszug> type = SynchronizeJobKontoauszug.class;
	    if (konto == null)
	    {
	      KontoAuswahlDialog d = new KontoAuswahlDialog(null,KontoFilter.createForeign(type),KontoAuswahlDialog.POSITION_CENTER);
	      konto = (Konto) d.open();
	    }
	    
	    if (konto == null)
	      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

			if (konto.isNewObject())
				konto.store();

      BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
      SynchronizeBackend backend = engine.getBackend(type,konto);
      SynchronizeJob job         = backend.create(type,konto);
      
      job.setContext(SynchronizeJob.CTX_ENTITY,konto);
      job.setContext(SynchronizeJobKontoauszug.CTX_FORCE_SALDO,true);
      job.setContext(SynchronizeJobKontoauszug.CTX_FORCE_UMSATZ,true);
      
      backend.execute(Arrays.asList(job));
		}
		catch (ApplicationException ae)
		{
		  throw ae;
		}
		catch (OperationCanceledException oce)
		{
		  // ignore
		}
		catch (Exception e)
		{
			Logger.error("unable to fetch bookings",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Abrufen der Umsätze"),StatusBarMessage.TYPE_ERROR));
		}
  }

}
