/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragDeleteJob.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/10/25 22:39:14 $
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
public class HBCIDauerauftragDeleteJob extends AbstractHBCIJob
{

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag) throws RemoteException, ApplicationException
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (auftrag == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

		if (!auftrag.isActive())
			throw new ApplicationException(i18n.tr("Dauerauftrag liegt nicht bei Bank vor und muss daher nicht online gelöscht werden"));

		if (auftrag.isNewObject())
			auftrag.store();

		this.dauerauftrag = auftrag;
		this.konto        = auftrag.getKonto();

		setJobParam("orderid",auftrag.getOrderID());
		// TODO: Beim Loeschen eines Dauerauftrags das Zieldatum konfigurierbar machen
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "DauerDel";
  }


	/**
	 * Prueft, ob das Loeschen bei der Bank erfolgreich war und loescht den
	 * Dauerauftrag anschliessend in der Datenbank.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
	void handleResult() throws ApplicationException, RemoteException
	{
		String statusText = getStatusText();

		String empfName = i18n.tr("an") + " " + dauerauftrag.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Löschen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Löschen des Dauerauftrags");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		konto.addToProtokoll(i18n.tr("Dauerauftrag gelöscht") + " " + empfName,Protokoll.TYP_SUCCESS);

		dauerauftrag.delete();

		Logger.info("dauerauftrag deleted successfully");
	}

}


/**********************************************************************
 * $Log: HBCIDauerauftragDeleteJob.java,v $
 * Revision 1.2  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/