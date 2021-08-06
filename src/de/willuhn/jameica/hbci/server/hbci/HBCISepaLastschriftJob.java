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

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV_Result.AbstractGVRLastSEPA;

import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Lastschrift".
 */
public class HBCISepaLastschriftJob extends AbstractHBCIJob
{

	private SepaLastschrift lastschrift = null;
	private SepaLastType type           = null;
	private Konto konto                 = null;

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaLastschriftJob(SepaLastschrift lastschrift) throws ApplicationException, RemoteException
	{
		try
		{
			if (lastschrift == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Auftrag an"));
		
			if (lastschrift.isNewObject())
			  lastschrift.store();
      
      if (lastschrift.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Auftrag wurde bereits ausgeführt"));

      this.lastschrift = lastschrift;
      this.konto       = this.lastschrift.getKonto();
      this.type        = this.lastschrift.getType();

      if (this.lastschrift.getBetrag() > Settings.getUeberweisungLimit())
        throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));

      org.kapott.hbci.structures.Konto own = Converter.HibiscusKonto2HBCIKonto(konto);
      // Deutsche Umlaute im eigenen Namen noch ersetzen
      // siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=16052
      own.name = HBCIProperties.replace(own.name,HBCIProperties.TEXT_REPLACEMENTS_SEPA);
			setJobParam("src",own);
			
      org.kapott.hbci.structures.Konto k = new org.kapott.hbci.structures.Konto();
      k.bic = lastschrift.getGegenkontoBLZ();
      k.iban = lastschrift.getGegenkontoNummer();
      k.name = lastschrift.getGegenkontoName();
      setJobParam("dst",k);
			
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      setJobParam("btg",lastschrift.getBetrag(),curr);
      
      String zweck = lastschrift.getZweck();
      if (zweck != null && zweck.length() > 0)
			  setJobParam("usage",zweck);
			
      String endToEndId = lastschrift.getEndtoEndId();
      if (endToEndId != null && endToEndId.trim().length() > 0)
        setJobParam("endtoendid",endToEndId);

      String pmtInfId = lastschrift.getPmtInfId();
      if (pmtInfId != null && pmtInfId.trim().length() > 0)
        setJobParam("pmtinfid", pmtInfId);

      String purp = lastschrift.getPurposeCode();
      if (purp != null && purp.length() > 0)
        setJobParam("purposecode",purp);

      setJobParam("mandateid",lastschrift.getMandateId());
      setJobParam("manddateofsig",lastschrift.getSignatureDate());
      setJobParam("creditorid",lastschrift.getCreditorId());
      setJobParam("sequencetype",lastschrift.getSequenceType().name());
      
      if (this.type != null)
        setJobParam("type",this.type.name());
      
      Date targetDate = lastschrift.getTargetDate();
      if (targetDate != null)
        setJobParam("targetdate",targetDate);
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
    return this.lastschrift;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    if (this.type != null)
      return this.type.getJobName();
    
    // Default CORE
    return SepaLastType.DEFAULT.getJobName();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("SEPA-Lastschrift an {0} (IBAN: {1})",lastschrift.getGegenkontoName(), lastschrift.getGegenkontoNummer());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    // Order-ID uebernehmen, wenn erhalten
    AbstractGVRLastSEPA result = (AbstractGVRLastSEPA) this.getJobResult();
    String orderId = result.getOrderId();
    Logger.info("orderid for job: " + orderId);
    lastschrift.setOrderId(orderId);
    lastschrift.store();

    // Als ausgefuehrt markieren (die Funktion macht intern bereits ein store())
    lastschrift.setAusgefuehrt(true);
    
    // Wenn wir eine zugeordnete Adresse haben, koennen wir den Sequenz-Type umsetzen
    String id = StringUtils.trimToNull(MetaKey.ADDRESS_ID.get(lastschrift));
    if (id != null)
    {
      try
      {
        HibiscusAddress ha = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,id);
        String seqCode = StringUtils.trimToNull(MetaKey.SEPA_SEQUENCE_CODE.get(ha));
        if (seqCode != null)
        {
          SepaLastSequenceType type = SepaLastSequenceType.valueOf(seqCode);
          if (type == SepaLastSequenceType.FRST)
          {
            Logger.debug("auto-switching sequence-code for address-id" + id + " from FRST to RCUR");
            MetaKey.SEPA_SEQUENCE_CODE.set(ha,SepaLastSequenceType.RCUR.name());
          }
        }
      }
      catch (IllegalArgumentException ie)
      {
        Logger.error("unable to determine enum value of SepaLastSequenceType",ie);
      }
      catch (ObjectNotFoundException e)
      {
        Logger.info("address-id " + id + " no longer exists, unable to auto-switch sequence-code");
      }
      catch (RemoteException re)
      {
        Logger.error("unable to to auto-switch sequence-code for address-id" + id,re);
      }
    }
    
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(lastschrift));
    konto.addToProtokoll(i18n.tr("SEPA-Lastschrift (Order-ID: {0}) eingereicht für: {1}",orderId, lastschrift.getGegenkontoNummer()),Protokoll.TYP_SUCCESS);
    Logger.info("sepa direct debit submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws ApplicationException, RemoteException
  {
    String msg = i18n.tr("Fehler beim Einziehen der SEPA-Lastschrift von {0}: {1}",lastschrift.getGegenkontoName(),error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  protected void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung der SEPA-Lastschrift {0} abgebrochen",lastschrift.getGegenkontoName());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }

}
