/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCILastschriftJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/19 00:16:04 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Lastschrift".
 */
public class HBCILastschriftJob extends AbstractHBCIJob
{

	private I18N i18n = null;
	private Lastschrift lastschrift = null;
	private Konto konto = null;

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCILastschriftJob(Lastschrift lastschrift) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (lastschrift == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Lastschrift an"));
		
			if (lastschrift.isNewObject())
				lastschrift.store();

			this.lastschrift = lastschrift;
			this.konto = lastschrift.getKonto();

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));

			setJobParam("btg",lastschrift.getBetrag(),konto.getWaehrung() == null ? "EUR" : konto.getWaehrung());

			Empfaenger empfaenger = (Empfaenger) Settings.getDBService().createObject(Empfaenger.class,null);
			empfaenger.setBLZ(lastschrift.getEmpfaengerBLZ());
			empfaenger.setKontonummer(lastschrift.getEmpfaengerKonto());
			empfaenger.setName(lastschrift.getEmpfaengerName());

			setJobParam("other",Converter.HibiscusEmpfaenger2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());

			setJobParam("usage",lastschrift.getZweck());

			String zweck2 = lastschrift.getZweck2();
			if (zweck2 != null && zweck2.length() > 0)
				setJobParam("usage_2",zweck2);
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
  String getIdentifier() {
    return "Last";
  }
  
  /**
   * Prueft, ob die lastschrift erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String statusText = getStatusText();

		String empfName = i18n.tr("von") + " " + lastschrift.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Lastschrift") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		konto.addToProtokoll(i18n.tr("Lastschrift ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);

		// Wir markieren die Ueberweisung als "ausgefuehrt"
		lastschrift.setAusgefuehrt();
		lastschrift.store();
		Logger.info("lastschrift submitted successfully");
  }
}


/**********************************************************************
 * $Log: HBCILastschriftJob.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/