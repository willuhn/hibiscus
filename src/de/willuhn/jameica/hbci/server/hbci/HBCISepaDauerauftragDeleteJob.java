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

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.HBCIJob;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.CanTermDelRestriction;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Dauerauftrag loeschen".
 */
public class HBCISepaDauerauftragDeleteJob extends AbstractHBCIJob
{
	private SepaDauerauftrag dauerauftrag = null;
	private Konto konto 						     	= null;
	private Date date                     = null;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   * @param date Datum, zu dem der Auftrag geloescht werden soll oder <code>null</code>
   * wenn zum naechstmoeglichen Zeitpunkt geloescht werden soll.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCISepaDauerauftragDeleteJob(SepaDauerauftrag auftrag, Date date) throws RemoteException, ApplicationException
	{
		try
		{
			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte w�hlen Sie einen SEPA-Dauerauftrag aus"));

			if (!auftrag.isActive())
				throw new ApplicationException(i18n.tr("Dauerauftrag liegt nicht bei der Bank vor und muss daher nicht online gel�scht werden"));

			if (auftrag.isNewObject())
				auftrag.store();

			this.dauerauftrag = auftrag;
			this.konto        = auftrag.getKonto();
			this.date         = date;

      String orderID = this.dauerauftrag.getOrderID();
      if (StringUtils.trimToEmpty(orderID).equals(BaseDauerauftrag.ORDERID_PLACEHOLDER))
        setJobParam("orderid",""); // Duerfen wir nicht mitschicken
      else
        setJobParam("orderid",orderID);

      org.kapott.hbci.structures.Konto own = Converter.HibiscusKonto2HBCIKonto(konto);
      // Deutsche Umlaute im eigenen Namen noch ersetzen
      // siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=16052
      own.name = HBCIProperties.replace(own.name,HBCIProperties.TEXT_REPLACEMENTS_SEPA);
      setJobParam("src",own);

      org.kapott.hbci.structures.Konto k = new org.kapott.hbci.structures.Konto();
      k.bic = dauerauftrag.getGegenkontoBLZ();
      k.iban = dauerauftrag.getGegenkontoNummer();
      k.name = dauerauftrag.getGegenkontoName();
      setJobParam("dst",k);

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      setJobParam("btg",dauerauftrag.getBetrag(),curr);

      setJobParamUsage(dauerauftrag);
      setJobParam("firstdate",dauerauftrag.getErsteZahlung());

      Date letzteZahlung = dauerauftrag.getLetzteZahlung();
      if (letzteZahlung != null)
        setJobParam("lastdate",letzteZahlung);

      Turnus turnus = dauerauftrag.getTurnus();
      setJobParam("timeunit",turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH ? "M" : "W");
      setJobParam("turnus",turnus.getIntervall());
      setJobParam("execday",turnus.getTag());
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
  
  @Override
  protected HibiscusDBObject getContext()
  {
    return this.dauerauftrag;
  }

  @Override
  public void setJob(HBCIJob job) throws RemoteException, ApplicationException
  {
    // Job-Restrictions checken, wenn ein Zieldatum angegeben ist.
    if (this.date != null)
    {
      Properties p = job.getJobRestrictions();
      Logger.info("target date for DauerSepaDel: " + this.date.toString());
      new CanTermDelRestriction(p).test(); // Test nur, wenn Datum angegeben
      this.setJobParam("date",this.date);
    }
    
    super.setJob(job);
  }
  
  @Override
  public String getIdentifier() {
    return "DauerSEPADel";
  }

  @Override
  public String getName() throws RemoteException
  {
    return i18n.tr("SEPA-Dauerauftrag an {0} l�schen",dauerauftrag.getGegenkontoName());
  }

  @Override
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    dauerauftrag.delete();
    konto.addToProtokoll(i18n.tr("SEPA-Dauerauftrag an {0} gel�scht",dauerauftrag.getGegenkontoName()),Protokoll.TYP_SUCCESS);
    Logger.info("sepa-dauerauftrag deleted successfully");
  }

  @Override
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim L�schen des SEPA-Dauerauftrages an {0}: {1}",new String[]{dauerauftrag.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
