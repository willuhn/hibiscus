/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.16 $
 * $Date: 2004/11/12 18:25:08 $
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
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Ueberweisung".
 */
public class HBCIUeberweisungJob extends AbstractHBCIJob
{

	private I18N i18n = null;
	private Ueberweisung ueberweisung = null;
	private Konto konto = null;

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIUeberweisungJob(Ueberweisung ueberweisung) throws ApplicationException, RemoteException
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (ueberweisung == null)
			throw new ApplicationException(i18n.tr("Bitte geben Sie eine Überweisung an"));
		
		if (ueberweisung.isNewObject())
			ueberweisung.store();

		this.ueberweisung = ueberweisung;
		this.konto = ueberweisung.getKonto();

		setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

		setJobParam("btg",ueberweisung.getBetrag(),konto.getWaehrung() == null ? "EUR" : konto.getWaehrung());

		Empfaenger empfaenger = (Empfaenger) Settings.getDBService().createObject(Empfaenger.class,null);
		empfaenger.setBLZ(ueberweisung.getEmpfaengerBLZ());
		empfaenger.setKontonummer(ueberweisung.getEmpfaengerKonto());
		empfaenger.setName(ueberweisung.getEmpfaengerName());

		setJobParam("dst",Converter.HibiscusEmpfaenger2HBCIKonto(empfaenger));
		setJobParam("name",empfaenger.getName());

		setJobParam("usage",ueberweisung.getZweck());

		String zweck2 = ueberweisung.getZweck2();
		if (zweck2 != null && zweck2.length() > 0)
			setJobParam("usage_2",zweck2);

	}

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  String getIdentifier() {
    return "Ueb";
  }
  
  /**
   * Prueft, ob die Ueberweisung erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String statusText = getStatusText();

		String empfName = i18n.tr("an") + " " + ueberweisung.getEmpfaengerName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Überweisung") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}

		konto.addToProtokoll(i18n.tr("Überweisung ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);

		// Wir markieren die Ueberweisung als "ausgefuehrt"
		ueberweisung.setAusgefuehrt();
		ueberweisung.store();
		Logger.info("ueberweisung submitted successfully");
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
 * Revision 1.16  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/26 23:47:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.12  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.10  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.9  2004/07/21 23:54:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.7  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.6  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.4  2004/04/27 22:30:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.2  2004/04/22 23:46:50  willuhn
 * @N UeberweisungJob
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/