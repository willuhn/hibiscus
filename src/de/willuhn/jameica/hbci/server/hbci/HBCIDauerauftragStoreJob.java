/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragStoreJob.java,v $
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
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Job fuer "Dauerauftrag bei der Bank speichern".
 * Der Job erkennt selbst, ob der Dauerauftrag bei der Bank neu angelegt werden
 * muss oder ob bereits einer vorliegt, der nur geaendert werden muss.
 */
public class HBCIDauerauftragStoreJob extends AbstractHBCIJob {

	private I18N i18n 								= null;
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

	private boolean active = false;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der bei der Bank gespeichert werden soll
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIDauerauftragStoreJob(Dauerauftrag auftrag) throws ApplicationException, RemoteException
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (auftrag == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

		if (auftrag.isNewObject())
			auftrag.store();

		this.dauerauftrag = auftrag;
		this.konto        = auftrag.getKonto();
		this.active				= auftrag.isActive();

		if (active)
			setJobParam("orderid",auftrag.getOrderID());

		setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

		setJobParam("btg.curr",konto.getWaehrung() == null ? "EUR" : konto.getWaehrung());

		setJobParam("btg.value",dauerauftrag.getBetrag());

		Empfaenger empfaenger = (Empfaenger) Settings.getDBService().createObject(Empfaenger.class,null);
		empfaenger.setBLZ(dauerauftrag.getEmpfaengerBLZ());
		empfaenger.setKontonummer(dauerauftrag.getEmpfaengerKonto());
		empfaenger.setName(dauerauftrag.getEmpfaengerName());
		setJobParam("dst",Converter.HibiscusEmpfaenger2HBCIKonto(empfaenger));

		setJobParam("name",empfaenger.getName());

		setJobParam("usage",dauerauftrag.getZweck());

		String zweck2 = dauerauftrag.getZweck2();
		if (zweck2 != null && zweck2.length() > 0)
			setJobParam("usage_2",zweck2);

		// So, jetzt noch der Turnus.
		// TODO Die Bank unterstuetzt ggf. nur einen Teil der von uns angebotenen Zahlungsrythmen.
		// Das sollte mal per job.getJobRestrictions() abgefragt und ausgefiltert werden.

		setJobParam("firstdate",dauerauftrag.getErsteZahlung());

		Date letzteZahlung = dauerauftrag.getLetzteZahlung();
		if (letzteZahlung != null)
			setJobParam("lastdate",letzteZahlung);

		Turnus t = dauerauftrag.getTurnus();
		setJobParam("timeunit",t.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH ? "M" : "W");
		setJobParam("turnus",t.getIntervall());
		setJobParam("execday",t.getTag());
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
		if (active)
			return "DauerEdit";
  	return "DauerNew";
  }

  /**
	 * Prueft, ob das Senden des Dauerauftrags erfolgreich war und speichert im
	 * Erfolgsfall die Order-ID.
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
	 */
	void handleResult() throws ApplicationException, RemoteException
	{
		String statusText = getStatusText();

		String empfName = i18n.tr("an") + " " + dauerauftrag.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Ausführen des Dauerauftrags");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		if (dauerauftrag.isActive())
			konto.addToProtokoll(i18n.tr("Dauerauftrag aktualisiert") + " " + empfName,Protokoll.TYP_SUCCESS);
		else
			konto.addToProtokoll(i18n.tr("Dauerauftrag ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);

		// jetzt muessen wir noch die Order-ID speichern, wenn er neu eingereicht wurde
		if (!active)
		{
			// Der Auftrag war neu, dann muessen wir noch die Order-ID speichern
			dauerauftrag.setOrderID(null); // TODO ORDER ID!!!
			dauerauftrag.store();
		}

		Logger.info("dauerauftrag submitted successfully");
	}

}


/**********************************************************************
 * $Log: HBCIDauerauftragStoreJob.java,v $
 * Revision 1.2  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/