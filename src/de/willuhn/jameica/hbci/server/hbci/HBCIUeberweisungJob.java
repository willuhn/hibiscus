/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/04/27 22:30:04 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Ueberweisung".
 */
public class HBCIUeberweisungJob extends AbstractHBCIJob {

	private I18N i18n = null;

	/**
	 * ct.
   * @param konto Konto, welches belastet wird.
   */
  public HBCIUeberweisungJob(Konto konto)
	{
		super(konto);

		try {
			setJobParam("src",Converter.JameicaKonto2HBCIKonto(konto));
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
			setJobParam("dst",Converter.JameicaEmpfaenger2HBCIKonto(empfaenger));
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
		if (!getJobResult().isOK())
		{
			throw new ApplicationException(
				statusText != null ?
					i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
					i18n.tr("Fehler beim Ausführen der Überweisung"));
		}
		Application.getLog().debug("ueberweisung sent successfully");
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
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