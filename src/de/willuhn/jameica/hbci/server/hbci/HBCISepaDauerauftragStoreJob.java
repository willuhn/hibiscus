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
import org.kapott.hbci.GV_Result.GVRDauerEdit;
import org.kapott.hbci.GV_Result.GVRDauerNew;
import org.kapott.hbci.GV_Result.HBCIJobResult;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.jameica.hbci.server.hbci.tests.TurnusRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Dauerauftrag bei der Bank speichern".
 * Der Job erkennt selbst, ob der Dauerauftrag bei der Bank neu angelegt werden
 * muss oder ob bereits einer vorliegt, der nur geaendert werden muss.
 */
public class HBCISepaDauerauftragStoreJob extends AbstractHBCIJob
{

	private SepaDauerauftrag dauerauftrag = null;
	private Konto konto 							    = null;

	private boolean active = false;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der bei der Bank gespeichert werden soll
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaDauerauftragStoreJob(SepaDauerauftrag auftrag) throws ApplicationException, RemoteException
	{
		try
		{
			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen SEPA-Dauerauftrag aus"));

			if (auftrag.isNewObject())
				auftrag.store();

			this.dauerauftrag = auftrag;
			this.konto        = auftrag.getKonto();
			this.active				= auftrag.isActive();

			if (!active)
      {
			  // Limit pruefen wir nur bei neuen Dauerauftraegen
        if (this.dauerauftrag.getBetrag() > Settings.getUeberweisungLimit())
          throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));
      }

      if (active)
      {
        String orderID = auftrag.getOrderID();
        if (StringUtils.trimToEmpty(orderID).equals(BaseDauerauftrag.ORDERID_PLACEHOLDER))
          setJobParam("orderid",""); // Duerfen wir nicht mitschicken
        else
          setJobParam("orderid",orderID);
        
      }

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

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",dauerauftrag.getBetrag(),curr);
			
      setJobParamUsage(dauerauftrag);
			setJobParam("firstdate",dauerauftrag.getErsteZahlung());

			String purp = dauerauftrag.getPurposeCode();
			if (purp != null && purp.length() > 0)
			  setJobParam("purposecode",purp);

			Date letzteZahlung = dauerauftrag.getLetzteZahlung();
			if (letzteZahlung != null)
				setJobParam("lastdate",letzteZahlung);

			Turnus turnus = dauerauftrag.getTurnus();
			setJobParam("timeunit",turnus.getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH ? "M" : "W");
			setJobParam("turnus",turnus.getIntervall());
			setJobParam("execday",turnus.getTag());
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
    return this.dauerauftrag;
  }

  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#setJob(org.kapott.hbci.GV.HBCIJob)
   */
  public void setJob(HBCIJob job) throws RemoteException, ApplicationException
  {
    // Tests fuer die Job-Restriktionen
    Properties p = job.getJobRestrictions();
    Turnus turnus = dauerauftrag.getTurnus();
    new TurnusRestriction(turnus,p).test();
    if (!active) // nur pruefen bei neuen Dauerauftraegen
      new PreTimeRestriction(dauerauftrag.getErsteZahlung(),p).test();
    
    super.setJob(job);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier() {
		if (active)
			return "DauerSEPAEdit";
  	return "DauerSEPANew";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    String empfName = dauerauftrag.getGegenkontoName();
    if (active)
      return i18n.tr("SEPA-Dauerauftrag an {0} aktualisieren",empfName);
    return i18n.tr("SEPA-Dauerauftrag an {0}",empfName);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    String empfName = dauerauftrag.getGegenkontoName();

    if (dauerauftrag.isActive())
      konto.addToProtokoll(i18n.tr("SEPA-Dauerauftrag aktualisiert an {0}",empfName),Protokoll.TYP_SUCCESS);
    else
      konto.addToProtokoll(i18n.tr("SEPA-Dauerauftrag ausgeführt an {0} ",empfName),Protokoll.TYP_SUCCESS);

    String orderID = null;
    HBCIJobResult result = this.getJobResult();
    if (result instanceof GVRDauerNew)
      orderID = ((GVRDauerNew)result).getOrderId();
    else
      orderID = ((GVRDauerEdit)result).getOrderId();
    
    if (StringUtils.trimToNull(orderID) == null)
    {
      Logger.warn("got no order id for this job, using placeholder id " + BaseDauerauftrag.ORDERID_PLACEHOLDER);
      konto.addToProtokoll(i18n.tr("Keine Order-ID für SEPA-Dauerauftrag von Bank erhalten",empfName),Protokoll.TYP_ERROR);
      orderID = BaseDauerauftrag.ORDERID_PLACEHOLDER;
    }
    dauerauftrag.setOrderID(orderID);
    dauerauftrag.store();

    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(dauerauftrag));

    Logger.info("dauerauftrag submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Ausführen des SEPA-Dauerauftrages an {0}: {1}",new String[]{dauerauftrag.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

}
