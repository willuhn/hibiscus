/**********************************************************************
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
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
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
      this.konto = this.lastschrift.getKonto();

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
      if (zweck != null && zweck.trim().length() > 0)
			  setJobParam("usage",zweck);
			
      String endToEndId = lastschrift.getEndtoEndId();
      if (endToEndId != null && endToEndId.trim().length() > 0)
        setJobParam("endtoendid",endToEndId);
      
      setJobParam("mandateid",lastschrift.getMandateId());
      setJobParam("manddateofsig",lastschrift.getSignatureDate());
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
  public String getIdentifier()
  {
    return "LastSEPA";
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
  void markExecuted() throws RemoteException, ApplicationException
  {
    lastschrift.setAusgefuehrt(true);
    
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(lastschrift));
    konto.addToProtokoll(i18n.tr("SEPA-Lastschrift eingezogen von: {0}",lastschrift.getGegenkontoNummer()),Protokoll.TYP_SUCCESS);
    Logger.info("sepa direct debit submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws ApplicationException, RemoteException
  {
    String msg = i18n.tr("Fehler beim Einziehen der SEPA-Lastschrift von {0}: {1}",lastschrift.getGegenkontoName(),error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung der SEPA-Lastschrift {0} abgebrochen",lastschrift.getGegenkontoName());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }

}
