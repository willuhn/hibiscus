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
import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.AuslandsUeberweisungDialog;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung einer Auslandsueberweisung verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
 */
public class AuslandsUeberweisungExecute implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erwartet ein Objekt vom Typ <code>AuslandsUeberweisung</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		if (context == null || !(context instanceof AuslandsUeberweisung))
			throw new ApplicationException(i18n.tr("Kein Auftrag angegeben"));

		try
		{
			final AuslandsUeberweisung u = (AuslandsUeberweisung) context;

			if (u.ausgefuehrt())
				throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

      Date termin = DateUtil.startOfDay(u.getTermin());
      Date now    = DateUtil.startOfDay(new Date());
      if (!u.isTerminUeberweisung() && (termin.getTime() - now.getTime()) >= (24 * 60 * 60 * 1000))
      {
        String q = i18n.tr("Der Termin liegt mindestens 1 Tag in Zukunft.\n" +
                           "Soll der Auftrag stattdessen als bankseitige SEPA-Terminüberweisung " +
                           "ausgeführt werden?");
        if (Application.getCallback().askUser(q))
        {
          u.setTerminUeberweisung(true);
          u.store();
        }
      }

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
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen der Überweisung"),StatusBarMessage.TYPE_ERROR));
				return;
			}

	    Konto konto = u.getKonto();
	    Class<SynchronizeJobSepaUeberweisung> type = SynchronizeJobSepaUeberweisung.class;

	    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
	    SynchronizeEngine engine   = bs.get(SynchronizeEngine.class);
	    SynchronizeBackend backend = engine.getBackend(type,konto);
	    SynchronizeJob job         = backend.create(type,konto);
	    
	    job.setContext(SynchronizeJob.CTX_ENTITY,u);
	    
	    backend.execute(Arrays.asList(job));
		}
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while executing transfer",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen der Überweisung: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}
