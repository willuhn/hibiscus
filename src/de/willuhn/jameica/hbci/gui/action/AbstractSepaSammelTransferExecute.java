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
import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SepaSammelTransferDialog;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die zur Ausfuehrung eines Sammel-Auftrages verwendet werden kann.
 * Er erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
 */
public abstract class AbstractSepaSammelTransferExecute implements Action
{

  /**
	 * Erwartet ein Objekt vom Typ <code>SammelTransfer</code> als Context.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (!(context instanceof SepaSammelTransfer))
      throw new ApplicationException(i18n.tr("Bitte geben Sie einen SEPA-Sammelauftrag an"));

		try
		{
			final SepaSammelTransfer u = (SepaSammelTransfer) context;

			if (u.ausgefuehrt())
        throw new ApplicationException(i18n.tr("SEPA-Sammelauftrag wurde bereits ausgeführt"));

			if (u.getBuchungen().size() == 0)
        throw new ApplicationException(i18n.tr("SEPA-Sammelauftrag enthält keine Buchungen"));

			if (u.isNewObject())
				u.store(); // wir speichern bei Bedarf selbst.

			if (u instanceof SepaSammelUeberweisung)
			{
			  SepaSammelUeberweisung su = (SepaSammelUeberweisung) u;
	      Date termin = DateUtil.startOfDay(su.getTermin());
	      Date now    = DateUtil.startOfDay(new Date());
	      if (!su.isTerminUeberweisung() && (termin.getTime() - now.getTime()) >= (24 * 60 * 60 * 1000))
	      {
	        String q = i18n.tr("Der Termin liegt mindestens 1 Tag in Zukunft.\n" +
	                           "Soll der Auftrag stattdessen als bankseitige SEPA-Sammelterminüberweisung " +
	                           "ausgeführt werden?");
	        if (Application.getCallback().askUser(q))
	        {
	          su.setTerminUeberweisung(true);
	          su.store();
	        }
	      }
			}

			SepaSammelTransferDialog d = new SepaSammelTransferDialog(u,SepaSammelTransferDialog.POSITION_CENTER);
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
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Sammel-Auftrages"));
				return;
			}
			execute(u);
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (Exception e)
    {
      Logger.error("error while executing sammelauftrag",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen des Sammel-Auftrages"),StatusBarMessage.TYPE_ERROR));
      return;
    }
  }

  /**
   * Wird aufgerufen, nachdem alle Sicherheitsabfragen erfolgt sind.
   * Hier muss die implementierende Klasse den Auftrag ausfuehren und dann zur
   * gewuenschten Zielseite wechseln.
   * @param transfer
   * @throws ApplicationException
   * @throws RemoteException
   */
  abstract void execute(SepaSammelTransfer transfer) throws ApplicationException, RemoteException;
}
