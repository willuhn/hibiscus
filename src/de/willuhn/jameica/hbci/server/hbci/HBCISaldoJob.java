/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISaldoJob.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/06/10 20:56:33 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Salden-Abfrage".
 */
public class HBCISaldoJob extends AbstractHBCIJob {

	private I18N i18n = null;

	/**
	 * ct.
   * @param konto
   */
  public HBCISaldoJob(Konto konto)
	{
		super(konto);

		try {
			setJobParam("my",Converter.JameicaKonto2HBCIKonto(konto));
		}
		catch (RemoteException e)
		{
			throw new RuntimeException("Fehler beim Setzen des Kontos");
		}

		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "SaldoReq";
  }

	/**
	 * Liefert den Saldo.
   * @return Saldo.
	 * @throws ApplicationException
   */
  public double getSaldo() throws ApplicationException
	{
		GVRSaldoReq result = (GVRSaldoReq) getJobResult();

		String statusText = getStatusText();
		if (!result.isOK())
		{
			String msg = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Abrufen des Saldos");

			addToProtokoll(i18n.tr("Fehler beim Abrufen das Saldos") + " ("+ msg +")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg);
		}
		Application.getLog().debug("job result is ok, returning saldo");
		addToProtokoll(i18n.tr("Saldo abgerufen"),Protokoll.TYP_SUCCESS);
		return result.getEntries()[0].ready.value.value;
	}
}


/**********************************************************************
 * $Log: HBCISaldoJob.java,v $
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