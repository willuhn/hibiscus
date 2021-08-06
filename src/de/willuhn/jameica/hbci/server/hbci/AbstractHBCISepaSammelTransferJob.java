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
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.BatchBookType;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Basis-Job fuer SEPA-Sammelauftraege.
 * @param <T> der konkrete Typ des SEPA-Sammelauftrages.
 */
public abstract class AbstractHBCISepaSammelTransferJob<T extends SepaSammelTransfer> extends AbstractHBCIJob
{

	private T transfer = null;
	private Konto konto = null;
	
  /**
	 * ct.
   * Achtung. Der Job-Parameter "data" fehlt noch und muss in den
   * abgeleiteten Klassen gesetzt werden.
   * @param transfer der auszufuehrende Sammel-Transfer.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public AbstractHBCISepaSammelTransferJob(T transfer) throws ApplicationException, RemoteException
	{
		try
		{
			if (transfer == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen SEPA-Sammelauftrag an"));
		
			if (transfer.isNewObject())
				transfer.store();

      if (transfer.ausgefuehrt())
        throw new ApplicationException(i18n.tr("SEPA-Sammelauftrag wurde bereits ausgeführt"));

			this.transfer = transfer;
			this.konto = transfer.getKonto();

			List<SepaSammelTransferBuchung> buchungen = this.transfer.getBuchungen();
			if (buchungen.size() == 0)
        throw new ApplicationException(i18n.tr("SEPA-Sammelauftrag enthält keine Buchungen"));
			
      for (SepaSammelTransferBuchung b:buchungen)
      {
        if (b.getBetrag() > Settings.getUeberweisungLimit())
          throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));
      }
      
      org.kapott.hbci.structures.Konto own = Converter.HibiscusKonto2HBCIKonto(konto);
      // Deutsche Umlaute im eigenen Namen noch ersetzen
      // siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=16052
      own.name = HBCIProperties.replace(own.name,HBCIProperties.TEXT_REPLACEMENTS_SEPA);
      setJobParam("src",own);
      
      BatchBookType batch = BatchBookType.byValue(MetaKey.SEPA_BATCHBOOK.get(this.transfer));
      if (batch != null && batch != BatchBookType.NONE)
        setJobParam("batchbook",batch.getValue());
      
      String pmtInfId = this.transfer.getPmtInfId();
      if (pmtInfId != null && pmtInfId.trim().length() > 0)
        setJobParam("pmtinfid", pmtInfId);

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      
      for (int i=0;i<buchungen.size();++i)
      {
        SepaSammelTransferBuchung b = buchungen.get(i);
        
        // Wir nehmen explizit ein Integer-Objekt, um sicherzugehen, dass
        // wir nicht durch Autoboxing die falsche Signatur erwischen
        Integer idx = Integer.valueOf(i);
        
        org.kapott.hbci.structures.Konto k = new org.kapott.hbci.structures.Konto();
        k.bic  = b.getGegenkontoBLZ();
        k.iban = b.getGegenkontoNummer();
        k.name = b.getGegenkontoName();
        
        setJobParam("dst", idx, k);
        setJobParam("btg", idx, b.getBetrag(),curr);
        
        String zweck = b.getZweck();
        if (zweck != null && zweck.length() > 0)
          setJobParam("usage", idx ,zweck);
        
        String endToEndId = b.getEndtoEndId();
        if (endToEndId != null && endToEndId.trim().length() > 0)
          setJobParam("endtoendid", idx, endToEndId);
        
        String purp = b.getPurposeCode();
        if (purp != null && purp.length() > 0)
          setJobParam("purposecode",idx, purp);
        
      }
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
    return this.getSammelTransfer();
  }

  /**
   * Liefert den Sammel-Transfer.
   * @return der Sammel-Transfer.
   */
  T getSammelTransfer()
  {
    return this.transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    transfer.setAusgefuehrt(true);

    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(transfer)); // BUGZILLA 545
    konto.addToProtokoll(i18n.tr("SEPA-Sammelauftrag [Bezeichnung: {0}] ausgeführt",transfer.getBezeichnung()),Protokoll.TYP_SUCCESS);
    Logger.info("sepa sammellastschrift submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Ausführen des SEPA-Sammelauftrages [Bezeichnung: {0}]: {1}",new String[]{transfer.getBezeichnung(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  protected void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung des SEPA-Sammelauftrages [Bezeichnung: {0}] abgebrochen",transfer.getBezeichnung());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }

}
