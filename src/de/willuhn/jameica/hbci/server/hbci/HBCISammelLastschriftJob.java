/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISammelLastschriftJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/05 19:11:25 $
 * $Author: web0 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Sammel-Lastschrift".
 */
public class HBCISammelLastschriftJob extends AbstractHBCIJob
{

	private I18N i18n = null;
	private SammelLastschrift lastschrift = null;
	private Konto konto = null;

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Sammel-Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISammelLastschriftJob(SammelLastschrift lastschrift) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (lastschrift == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Sammel-Lastschrift an"));
		
			if (lastschrift.isNewObject())
				lastschrift.store();

			this.lastschrift = lastschrift;
			this.konto = lastschrift.getKonto();

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
			setJobParam("data",Converter.HibiscusSammelLastschrift2DTAUS(lastschrift).toString());

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
    return "MultiLast";
  }
  
  /**
   * Prueft, ob die Samml-Lastschrift erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String statusText = getStatusText();

		String empfName = i18n.tr("an Konto") + " " + lastschrift.getKonto().getBezeichnung();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Sammel-Lastschrift") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}


		// Wir markieren die Ueberweisung als "ausgefuehrt"
		lastschrift.setAusgefuehrt();
    konto.addToProtokoll(i18n.tr("Sammel-Lastschrift ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);
		Logger.info("sammellastschrift submitted successfully");
  }
}


/**********************************************************************
 * $Log: HBCISammelLastschriftJob.java,v $
 * Revision 1.1  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 **********************************************************************/