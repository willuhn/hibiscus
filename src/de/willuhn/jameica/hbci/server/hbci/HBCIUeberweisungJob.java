/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.40 $
 * $Date: 2008/11/17 23:30:00 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Ueberweisung".
 */
public class HBCIUeberweisungJob extends AbstractHBCIJob
{

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
    super();
		try
		{

			if (ueberweisung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Überweisung an"));
		
			if (ueberweisung.isNewObject())
				ueberweisung.store();
      
      if (ueberweisung.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

      this.ueberweisung = ueberweisung;
      this.konto = this.ueberweisung.getKonto();
      this.isTermin = this.ueberweisung.isTerminUeberweisung();

      if (this.ueberweisung.getBetrag() > Settings.getUeberweisungLimit())
        throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));

			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",ueberweisung.getBetrag(),curr);
      
      String key = this.ueberweisung.getTextSchluessel();
      if (key != null && key.length() > 0)
        setJobParam("key",key);

			HibiscusAddress empfaenger = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
			empfaenger.setBlz(ueberweisung.getGegenkontoBLZ());
			empfaenger.setKontonummer(ueberweisung.getGegenkontoNummer());
			empfaenger.setName(ueberweisung.getGegenkontoName());

			setJobParam("dst",Converter.Address2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());

			setJobParam("usage",ueberweisung.getZweck());

			String zweck2 = ueberweisung.getZweck2();
			if (zweck2 != null && zweck2.length() > 0)
				setJobParam("usage_2",zweck2);
      
      GenericIterator moreUsages = ueberweisung.getWeitereVerwendungszwecke();
      int pos = 3;
      while (moreUsages != null && moreUsages.hasNext())
      {
        Verwendungszweck zweck = (Verwendungszweck) moreUsages.next();
        String text = zweck.getText();
        if (text == null || text.length() == 0)
          continue;
        setJobParam("usage_" + pos,text);
        pos++;
      }

      if (isTermin)
      {
        Date d = this.ueberweisung.getTermin();
        setJobParam("date",d);

        Properties p = HBCIFactory.getInstance().getJobRestrictions(this.konto,this);
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Überweisung an {0}",ueberweisung.getGegenkontoName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    ueberweisung.setAusgefuehrt();
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueberweisung)); // BUGZILLA 501
    konto.addToProtokoll(i18n.tr("Überweisung ausgeführt an: {0}",ueberweisung.getGegenkontoName()),Protokoll.TYP_SUCCESS);
    Logger.info("ueberweisung submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws ApplicationException, RemoteException
  {
    String msg = i18n.tr("Fehler beim Ausführen der Überweisung an {0}: {1}",new String[]{ueberweisung.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}


/**********************************************************************
 * $Log: HBCIUeberweisungJob.java,v $
 * Revision 1.40  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.39  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.38  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.37  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.36  2007/12/06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.35  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.34  2007/11/11 19:44:28  willuhn
 * @N Bug 501
 *
 * Revision 1.33  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/