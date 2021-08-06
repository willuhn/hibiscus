/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Auslandsueberweisung".
 */
public class HBCIAuslandsUeberweisungJob extends AbstractHBCIJob
{

	private AuslandsUeberweisung ueberweisung = null;
  private boolean isTermin                  = false;
  private boolean isUmb                     = false;
	private Konto konto                       = null;

  /**
	 * ct.
   * @param ueberweisung die auszufuehrende Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIAuslandsUeberweisungJob(AuslandsUeberweisung ueberweisung) throws ApplicationException, RemoteException
	{
		try
		{
			if (ueberweisung == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Auftrag an"));
		
			if (ueberweisung.isNewObject())
				ueberweisung.store();
      
      if (ueberweisung.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt"));

      this.ueberweisung = ueberweisung;
      this.konto        = this.ueberweisung.getKonto();
      this.isTermin     = this.ueberweisung.isTerminUeberweisung();
      this.isUmb        = this.ueberweisung.isUmbuchung();

      if (this.ueberweisung.getBetrag() > Settings.getUeberweisungLimit())
        throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));

      org.kapott.hbci.structures.Konto own = Converter.HibiscusKonto2HBCIKonto(konto);
      // Deutsche Umlaute im eigenen Namen noch ersetzen
      // siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=16052
      own.name = HBCIProperties.replace(own.name,HBCIProperties.TEXT_REPLACEMENTS_SEPA);
			setJobParam("src",own);
			
      org.kapott.hbci.structures.Konto k = new org.kapott.hbci.structures.Konto();
      k.bic = ueberweisung.getGegenkontoBLZ();
      k.iban = ueberweisung.getGegenkontoNummer();
      k.name = ueberweisung.getGegenkontoName();
      setJobParam("dst",k);
			
      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      setJobParam("btg",ueberweisung.getBetrag(),curr);
      
      String zweck = ueberweisung.getZweck();
      if (zweck != null && zweck.length() > 0)
			  setJobParam("usage",zweck);
			
      String endToEndId = ueberweisung.getEndtoEndId();
      if (endToEndId != null && endToEndId.trim().length() > 0)
        setJobParam("endtoendid",endToEndId);
      
      String pmtInfId = ueberweisung.getPmtInfId();
      if (pmtInfId != null && pmtInfId.trim().length() > 0)
        setJobParam("pmtinfid", pmtInfId);

      String purp = ueberweisung.getPurposeCode();
      if (purp != null && purp.length() > 0)
        setJobParam("purposecode",purp);

		}
		catch (ApplicationException | RemoteException e)
		{
			throw e;
		}
		catch (Throwable t)
		{
			Logger.error("error while executing job " + getIdentifier(),t);
			throw new ApplicationException(i18n.tr("Fehler beim Erstellen des Auftrags. Fehlermeldung: {0}",t.getMessage()),t);
		}
	}
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getContext()
   */
  @Override
  protected HibiscusDBObject getContext()
  {
    return this.ueberweisung;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    if (isTermin)
      return "TermUebSEPA";
    if (isUmb)
      return "UmbSEPA";
    
    return "UebSEPA";
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("SEPA-Überweisung an {0} (IBAN: {1})",new String[]{ueberweisung.getGegenkontoName(), ueberweisung.getGegenkontoNummer()});
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    ueberweisung.setAusgefuehrt(true);
    
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueberweisung));
    konto.addToProtokoll(i18n.tr("SEPA-Überweisung ausgeführt an: {0}",ueberweisung.getGegenkontoNummer()),Protokoll.TYP_SUCCESS);
    Logger.info("foreign transfer submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws ApplicationException, RemoteException
  {
    String msg = i18n.tr("Fehler beim Ausführen des Auftrages an {0}: {1}",new String[]{ueberweisung.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  protected void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung des Auftrages an {0} abgebrochen",ueberweisung.getGegenkontoName());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }

}
