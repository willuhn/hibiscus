/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISaldoJob.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/11/13 17:02:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import org.kapott.hbci.GV_Result.GVRSaldoReq;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Salden-Abfrage".
 */
public class HBCISaldoJob extends AbstractHBCIJob {

	private Konto konto = null;
	private I18N i18n = null;

  /**
	 * ct.
   * @param konto konto, fuer das der Saldo ermittelt werden soll.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISaldoJob(Konto konto) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (ApplicationException e2)
		{
			throw e2;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier()
  {
    return "SaldoReq";
  }

  /**
   * Prueft, ob das Abrufen des Saldo erfolgreich war.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws RemoteException, ApplicationException
  {
		GVRSaldoReq result = (GVRSaldoReq) getJobResult();

		String statusText = getStatusText();
		if (!result.isOK())
		{
			String msg = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Abrufen des Saldos");

			konto.addToProtokoll(i18n.tr("Fehler beim Abrufen das Saldos") + " ("+ msg +")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg);
		}

		konto.addToProtokoll(i18n.tr("Saldo abgerufen"),Protokoll.TYP_SUCCESS);

		// Jetzt speichern wir noch den neuen Saldo.
		konto.setSaldo(result.getEntries()[0].ready.value.value);
		konto.store();
		Logger.info("saldo fetched successfully");
  }
}


/**********************************************************************
 * $Log: HBCISaldoJob.java,v $
 * Revision 1.15  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.14  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.11  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.9  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.8  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.6  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/06/10 20:56:33  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.3  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.2  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/