/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.27 $
 * $Date: 2006/03/15 17:28:41 $
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
import java.util.Enumeration;
import java.util.Properties;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
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
  private boolean isTermin = false;
	private Konto konto = null;

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIUeberweisungJob(Ueberweisung ueberweisung) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (ueberweisung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Überweisung an"));
		
			if (ueberweisung.isNewObject())
				ueberweisung.store();

      this.ueberweisung = ueberweisung;
      this.konto = this.ueberweisung.getKonto();
      this.isTermin = this.ueberweisung.isTerminUeberweisung();

			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",ueberweisung.getBetrag(),curr);

			Adresse empfaenger = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
			empfaenger.setBLZ(ueberweisung.getGegenkontoBLZ());
			empfaenger.setKontonummer(ueberweisung.getGegenkontoNummer());
			empfaenger.setName(ueberweisung.getGegenkontoName());

			setJobParam("dst",Converter.HibiscusAdresse2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());

			setJobParam("usage",ueberweisung.getZweck());

			String zweck2 = ueberweisung.getZweck2();
			if (zweck2 != null && zweck2.length() > 0)
				setJobParam("usage_2",zweck2);

      if (isTermin)
      {
        Date d = this.ueberweisung.getTermin();
        setJobParam("date",d);

        Passport passport = PassportRegistry.findByClass(this.konto.getPassportClass());
        // BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
        passport.init(this.konto);

        Properties p = HBCIFactory.getInstance().getJobRestrictions(this,passport.getHandle());
        Enumeration keys = p.keys();
        while (keys.hasMoreElements())
        {
          String s = (String) keys.nextElement();
          Logger.info("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
        }
        new PreTimeRestriction(d,p).test();
      }
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
  String getIdentifier()
  {
    return isTermin ? "TermUeb" : "Ueb";
  }
  
  /**
   * Prueft, ob die Ueberweisung erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String empfName = ueberweisung.getGegenkontoName();

		if (!getJobResult().isOK())
		{

			String msg = i18n.tr("Fehler beim Ausführen der Überweisung an {0}",empfName);


			String error = getStatusText();

			konto.addToProtokoll(msg + ": " + error,Protokoll.TYP_ERROR);
			throw new ApplicationException(msg + ": " + error);
		}

		// Wir markieren die Ueberweisung als "ausgefuehrt"
		ueberweisung.setAusgefuehrt();
    konto.addToProtokoll(i18n.tr("Überweisung ausgeführt") + " " + empfName,Protokoll.TYP_SUCCESS);
		Logger.info("ueberweisung submitted successfully");
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
 * Revision 1.27  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.26  2005/11/18 00:28:20  willuhn
 * @R removed test code
 *
 * Revision 1.25  2005/11/14 13:38:43  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.24  2005/11/14 13:08:11  willuhn
 * @N Termin-Ueberweisungen
 *
 * Revision 1.23  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.22  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.21  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.20  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.19  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2005/02/02 18:19:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
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