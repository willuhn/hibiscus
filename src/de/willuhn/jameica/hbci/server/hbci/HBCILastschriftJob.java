/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCILastschriftJob.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/03/30 23:26:28 $
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
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
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

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",lastschrift.getBetrag(),curr);

			// BUGZILLA #8 http://www.willuhn.de/bugzilla/show_bug.cgi?id=8
			if (lastschrift.getTyp() != null)
				setJobParam("type",lastschrift.getTyp());

			Adresse empfaenger = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
			empfaenger.setBLZ(lastschrift.getGegenkontoBLZ());
			empfaenger.setKontonummer(lastschrift.getGegenkontoNummer());
			empfaenger.setName(lastschrift.getGegenkontoName());

			setJobParam("other",Converter.HibiscusAdresse2HBCIKonto(empfaenger));
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

		String empfName = i18n.tr("von") + " " + lastschrift.getGegenkontoName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Lastschrift") + " " + empfName;


			String error = (statusText != null) ?
										i18n.tr("Fehlermeldung der Bank") + ": " + statusText :
										i18n.tr("Unbekannter Fehler");

			konto.addToProtokoll(msg + " ("+error+")",Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + " ("+error+")");
		}


		// Wir markieren die Ueberweisung als "ausgefuehrt"
		lastschrift.setAusgefuehrt();
    konto.addToProtokoll(i18n.tr("Lastschrift ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);
		Logger.info("lastschrift submitted successfully");
  }
}


/**********************************************************************
 * $Log: HBCILastschriftJob.java,v $
 * Revision 1.6  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.2  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/