/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragDeleteJob.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/25 17:58:56 $
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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Job fuer "Dauerauftrag loeschen".
 */
public class HBCIDauerauftragDeleteJob extends AbstractHBCIJob {

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

	/**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag) throws RemoteException
	{
		this.dauerauftrag = auftrag;
		this.konto        = auftrag.getKonto();

		setJobParam("orderid",auftrag.getOrderID());
		// TODO: Beim Loeschen eines Dauerauftrags das Zieldatum konfigurierbar machen

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "DauerDel";
  }


	/**
	 * Prueft, ob das Loeschen des Dauerauftrags erfolgreich war.
	 * War es das nicht, wird eine ApplicationException mit der Fehlermeldung
	 * der Bank geliefert.
	 * @throws ApplicationException Wenn bei der Ueberweisung ein Fehler auftrat.
	 */
	public void check() throws ApplicationException
	{
		String statusText = getStatusText();

		String empfName = "";
		try {
			empfName = i18n.tr("an") + " " + dauerauftrag.getEmpfaengerName();
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading empfaenger name",e);
		}

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Löschen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Löschen des Dauerauftrags");

			try {
				konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			}
			catch (RemoteException e)
			{
				Logger.error("error while writing protocol",e);
			}
			throw new ApplicationException(msg + " ("+error+")");
		}
		try {
			konto.addToProtokoll(i18n.tr("Dauerauftrag gelöscht") + " " + empfName,Protokoll.TYP_SUCCESS);
		}
		catch (RemoteException e)
		{
			Logger.error("error while writing protocol",e);
		}
		Logger.debug("dauerauftrag deleted successfully");
	}

}


/**********************************************************************
 * $Log: HBCIDauerauftragDeleteJob.java,v $
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/