/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragStoreJob.java,v $
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
   */
  public HBCIDauerauftragStoreJob(Dauerauftrag auftrag) throws RemoteException
	{
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

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier() {
		if (active)
			return "DauerEdit";
  	return "DauerNew";
  }

	/**
	 * Liefert die neu vergebene oder bereits vorhandene Auftragsnummer.
	 * Wird der Dauerauftrag neu eingereicht, dann erhalten wir hier eine
	 * Auftragsnummer zurueck.
   * @return
   */
  public String getOrderID()
	{
		return null; // TODO ORDER_ID zurueckliefern
	}

	/**
	 * Prueft, ob das Speichern des Dauerauftrags erfolgreich war.
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

			String msg = i18n.tr("Fehler beim Ausführen des Dauerauftrages") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Ausführen des Dauerauftrags");

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
			if (dauerauftrag.isActive())
				konto.addToProtokoll(i18n.tr("Dauerauftrag aktualisiert") + " " + empfName,Protokoll.TYP_SUCCESS);
			else
				konto.addToProtokoll(i18n.tr("Dauerauftrag ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);
		}
		catch (RemoteException e)
		{
			Logger.error("error while writing protocol",e);
		}
		Logger.debug("dauerauftrag submitted successfully");
	}

}


/**********************************************************************
 * $Log: HBCIDauerauftragStoreJob.java,v $
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/