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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobQuittung;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum manuellen Senden der Empfangsquittung.
 */
public class KontoKontoauszugReceipt implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
	 * Erwartet ein Objekt vom Typ <code>Konto</code> als Context.
	 * Fehlt das Konto, dann wird es in einem Dialog abgefragt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (!(context instanceof Kontoauszug))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Kontoauszug aus"));

		try
		{
	    Kontoauszug ka = (Kontoauszug) context;
	    Konto k = ka.getKonto();
	    
	    Class<SynchronizeJobQuittung> type = SynchronizeJobQuittung.class;

	    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
	    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
	    SynchronizeBackend backend = engine.getBackend(type,k);
	    SynchronizeJob job         = backend.create(type,k);
	    
	    job.setContext(SynchronizeJob.CTX_ENTITY,ka);
	    
	    backend.execute(Arrays.asList(job));
		}
		catch (ApplicationException ae)
		{
		  throw ae;
		}
		catch (RemoteException re)
		{
		  Logger.error("unable to send receipt",re);
		  throw new ApplicationException(i18n.tr("Senden der Empfangsquittung fehlgeschlagen: {0}",re.getMessage()));
		}
  }

}
