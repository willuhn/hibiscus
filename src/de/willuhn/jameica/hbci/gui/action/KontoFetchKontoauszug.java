/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszugPdf;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die die elektronischen Kontoauszuege eines Kontos abruft.
 * Er erwartet ein Objekt vom Typ <code>Konto</code> als Context.
 */
public class KontoFetchKontoauszug implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
	 * Fehlt das Konto, dann wird es in einem Dialog abgefragt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Konto k = null;
    if (context instanceof Konto)
      k = (Konto) context;
    
    // Wir zeigen den Dialog immer an. Auch wenn ein Konto uebergeben wurde.
    // Wenn aber eins angegeben ist, waehlen wir es vor.
    // 1) Wir zeigen einen Dialog an, in dem der User das Konto auswaehlt
    KontoAuswahlDialog d = new KontoAuswahlDialog(k,KontoFilter.createForeign(SynchronizeJobKontoauszugPdf.class),KontoAuswahlDialog.POSITION_CENTER);
    try
    {
      k = (Konto) d.open();
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

		if (k == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    Class<SynchronizeJobKontoauszugPdf> type = SynchronizeJobKontoauszugPdf.class;

    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
    SynchronizeBackend backend = engine.getBackend(type,k);
    SynchronizeJob job         = backend.create(type,k);
    
    job.setContext(SynchronizeJob.CTX_ENTITY,k);
    job.setContext(SynchronizeJobKontoauszugPdf.CTX_FORCE,true);
    
    backend.execute(Arrays.asList(job));
  }

}
