/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.8 $
 * $Date: 2004/07/14 23:48:31 $
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

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Job fuer "Ueberweisung".
 */
public class HBCIUeberweisungJob extends AbstractHBCIJob {

	private I18N i18n = null;
	private Empfaenger empfaenger = null;

	/**
	 * ct.
   * @param konto Konto, welches belastet wird.
   */
  public HBCIUeberweisungJob(Konto konto)
	{
		super(konto);

		try {
			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));
			setJobParam("btg.curr",konto.getWaehrung() == null ? "EUR" : konto.getWaehrung());

		}
		catch (RemoteException e)
		{
			throw new RuntimeException("Fehler beim Setzen von Absenderkonto/Währung");
		}

		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
	}

	/**
	 * Speichert den Empfaenger der Ueberweisung.
   * @param empfaenger der Empfaenger.
   */
  public void setEmpfaenger(Empfaenger empfaenger)
	{
		try {
			this.empfaenger = empfaenger;
			setJobParam("dst",Converter.HibiscusEmpfaenger2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());
		}
		catch (RemoteException e)
		{
			throw new RuntimeException("Fehler beim Setzen des Empfaengers");
		}
	}

	/**
	 * Speichert die Zeile 1 des Ueberweisungszwecks.
   * @param zweck Ueberweisungszweck
   */
  public void setZweck(String zweck)
	{
		setJobParam("usage",zweck);
	}

	/**
	 * Speichert die Zeile 2 des Ueberweisungszwecks.
	 * @param zweck2 Ueberweisungszweck
	 */
	public void setZweck2(String zweck2)
	{
		if (zweck2 == null || zweck2.length() == 0)
			return;
		setJobParam("usage_2",zweck2);
	}

	/**
	 * Speichert den Betrag der Ueberweisung.
   * @param betrag Betrag der Ueberweisung.
   */
  public void setBetrag(double betrag)
	{
		setJobParam("btg.value",HBCI.DECIMALFORMAT.format(betrag));
	}

  /**
   * @see de.willuhn.jameica.hbci.rmi.hbci.HBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "Ueb";
  }
  
  /**
   * Prueft, ob die Ueberweisung erfolgreich war.
   * War sie das nicht, wird eine ApplicationException mit der Fehlermeldung
   * der Bank geliefert.
   * @throws ApplicationException Wenn bei der Ueberweisung ein Fehler auftrat.
   */
  public void check() throws ApplicationException
  {
		String statusText = getStatusText();

		String empfName = "";
		try {
			empfName = i18n.tr("an") + " " + empfaenger.getName();
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading empfaenger name",e);
		}

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Überweisung") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler beim Ausführen der Überweisung");

			try {
				getKonto().addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			}
			catch (RemoteException e)
			{
				Logger.error("error while writing protocol",e);
			}
			throw new ApplicationException(msg + " ("+error+")");
		}
		try {
			getKonto().addToProtokoll(i18n.tr("Überweisung ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);
		}
		catch (RemoteException e)
		{
			Logger.error("error while writing protocol",e);
		}
		Logger.debug("ueberweisung sent successfully");
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
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