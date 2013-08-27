/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIUeberweisungJob.java,v $
 * $Revision: 1.51 $
 * $Date: 2012/03/01 22:19:15 $
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
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
	private boolean isUmb    = false;
	private boolean isBzu    = false;
	private boolean isSpende = false;
	
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
			if (ueberweisung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Überweisung an"));
		
			if (ueberweisung.isNewObject())
				ueberweisung.store();
      
      if (ueberweisung.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Überweisung wurde bereits ausgeführt"));

      this.ueberweisung = ueberweisung;
      this.konto = this.ueberweisung.getKonto();
      
      this.isTermin = this.ueberweisung.isTerminUeberweisung();
      this.isUmb    = this.ueberweisung.isUmbuchung();

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
      {
        setJobParam("key",key);
        this.isBzu    = key.equals(TextSchluessel.TS_BZU);
        this.isSpende = key.equals(TextSchluessel.TS_SPENDE);
      }

			HibiscusAddress empfaenger = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
			empfaenger.setBlz(ueberweisung.getGegenkontoBLZ());
			empfaenger.setKontonummer(ueberweisung.getGegenkontoNummer());
			empfaenger.setName(ueberweisung.getGegenkontoName());

			setJobParam("dst",Converter.Address2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());
			setJobParamUsage(ueberweisung);
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#setJob(org.kapott.hbci.GV.HBCIJob)
   */
  public void setJob(HBCIJob job) throws RemoteException, ApplicationException
  {
    if (this.isTermin)
    {
      Date date = this.ueberweisung.getTermin();
      Properties p = job.getJobRestrictions();
      new PreTimeRestriction(date,p).test();
      this.setJobParam("date",date);
    }

    super.setJob(job);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#setJobParamUsage(de.willuhn.jameica.hbci.rmi.Transfer)
   */
  void setJobParamUsage(Transfer t) throws RemoteException
  {
    // Wir koennen hier direkt unsere Member-Ueberweisung nehmen.
    // Bei der kennen wir auch schon den Typ.
    if (this.isBzu)
    {
      // Bei BZU muessen in der ersten Zeile Verwendungszweck zwingend die BZU-Daten stehen.
      // Das ist eine 13-stellige Checksumme, die im Job unter "bzudata" gespeichert wird.
      // Die restlichen Zeilen Verwendungszweck kommen dann hinten dran ab usage_2
      setJobParam("bzudata",this.ueberweisung.getZweck());
      
      String line2 = this.ueberweisung.getZweck2();
      String[] moreLines = this.ueberweisung.getWeitereVerwendungszwecke();
      List<String> lines = new ArrayList<String>();
      if (line2 != null && line2.trim().length() > 0) lines.add(line2.trim());
      if (moreLines != null && moreLines.length > 0)
      {
        for (String s:moreLines)
        {
          if (s == null || s.trim().length() == 0)
            continue;
          lines.add(s.trim());
        }
      }
      for (int i=0;i<lines.size();++i)
      {
        setJobParam("usage_" + (i+2),lines.get(i)); // wir beginnen mit "usage_2"
      }
    }
    else if (this.isSpende)
    {
      // Bei Spenden-Ueberweisung duerfen nur genau 3 Zeilen Verwendungszweck enthalten
      // sein. Und das muessen "spenderid", "plz_street" und "name_ort" sein. Weitere
      // Zeilen sind nicht zulaessig.
      setJobParam("spenderid",this.ueberweisung.getZweck());
      setJobParam("plz_street",this.ueberweisung.getZweck2());
      String[] wvz = this.ueberweisung.getWeitereVerwendungszwecke();
      if (wvz != null && wvz.length > 0)
        setJobParam("name_ort",wvz[0]);
    }
    else
    {
      // Ueberweisung mit regulaerem Verwendungszweck
      super.setJobParamUsage(t);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    if (isTermin)
      return "TermUeb";
    if (isUmb)
      return "Umb";
    if (isBzu)
      return "UebBZU";
    if (isSpende)
      return "Donation";
    return "Ueb";
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    if (this.isBzu)
      return i18n.tr("BZÜ-Überweisung an {0}",ueberweisung.getGegenkontoName());
    if (this.isSpende)
      return i18n.tr("Spenden-Überweisung an {0}",ueberweisung.getGegenkontoName());
    return i18n.tr("Überweisung an {0}",ueberweisung.getGegenkontoName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    ueberweisung.setAusgefuehrt(true);
    
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

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung der Überweisung an {0} abgebrochen",ueberweisung.getGegenkontoName());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }
  
  
}
