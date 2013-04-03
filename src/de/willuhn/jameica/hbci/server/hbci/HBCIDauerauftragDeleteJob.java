/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragDeleteJob.java,v $
 * $Revision: 1.22 $
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
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.HBCIJob;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.CanTermDelRestriction;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Dauerauftrag loeschen".
 */
public class HBCIDauerauftragDeleteJob extends AbstractHBCIJob
{
	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;
	private Date date                 = null;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der geloescht werden soll
   * @param date Datum, zu dem der Auftrag geloescht werden soll oder <code>null</code>
   * wenn zum naechstmoeglichen Zeitpunkt geloescht werden soll.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public HBCIDauerauftragDeleteJob(Dauerauftrag auftrag, Date date) throws RemoteException, ApplicationException
	{
		try
		{
			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

			if (!auftrag.isActive())
				throw new ApplicationException(i18n.tr("Dauerauftrag liegt nicht bei der Bank vor und muss daher nicht online gelöscht werden"));

			if (auftrag.isNewObject())
				auftrag.store();

			this.dauerauftrag = auftrag;
			this.konto        = auftrag.getKonto();
			this.date         = date;

      String orderID = this.dauerauftrag.getOrderID();
      if (StringUtils.trimToEmpty(orderID).equals(Dauerauftrag.ORDERID_PLACEHOLDER))
        setJobParam("orderid",""); // Duerfen wir nicht mitschicken
      else
        setJobParam("orderid",orderID);

      setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      setJobParam("btg",dauerauftrag.getBetrag(),curr);

      HibiscusAddress empfaenger = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
      empfaenger.setBlz(dauerauftrag.getGegenkontoBLZ());
      empfaenger.setKontonummer(dauerauftrag.getGegenkontoNummer());
      empfaenger.setName(dauerauftrag.getGegenkontoName());
      setJobParam("dst",Converter.Address2HBCIKonto(empfaenger));
      setJobParam("name",empfaenger.getName());

      setJobParam("usage",dauerauftrag.getZweck());

      String zweck2 = dauerauftrag.getZweck2();
      if (zweck2 != null)
        zweck2 = zweck2.trim(); // BUGZILLA 517
      if (zweck2 != null && zweck2.length() > 0)
        setJobParam("usage_2",zweck2);

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

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#setJob(org.kapott.hbci.GV.HBCIJob)
   */
  public void setJob(HBCIJob job) throws RemoteException, ApplicationException
  {
    // Job-Restrictions checken, wenn ein Zieldatum angegeben ist.
    if (this.date != null)
    {
      Properties p = job.getJobRestrictions();
      Enumeration keys = p.keys();
      while (keys.hasMoreElements())
      {
        String s = (String) keys.nextElement();
        Logger.debug("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
      }

      Logger.info("target date for DauerDel: " + this.date.toString());
      new CanTermDelRestriction(p).test(); // Test nur, wenn Datum angegeben
      this.setJobParam("date",this.date);
    }
    
    super.setJob(job);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier() {
    return "DauerDel";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Dauerauftrag an {0} löschen",dauerauftrag.getGegenkontoName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    dauerauftrag.delete();
    konto.addToProtokoll(i18n.tr("Dauerauftrag an {0} gelöscht",dauerauftrag.getGegenkontoName()),Protokoll.TYP_SUCCESS);
    Logger.info("dauerauftrag deleted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Löschen des Dauerauftrages an {0}: {1}",new String[]{dauerauftrag.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
