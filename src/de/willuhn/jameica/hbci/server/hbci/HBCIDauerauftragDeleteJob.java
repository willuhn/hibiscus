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
      
			if (date != null)
			{
				Properties p = HBCIFactory.getInstance().getJobRestrictions(this.konto,this);
				Enumeration keys = p.keys();
				while (keys.hasMoreElements())
				{
					String s = (String) keys.nextElement();
					Logger.debug("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
				}

				Logger.info("target date for DauerDel: " + date.toString());
				new CanTermDelRestriction(p).test(); // Test nur, wenn Datum angegeben
				setJobParam("date",date);
			}

			// Den brauchen wir, damit das Loeschen funktioniert.
			HBCIFactory.getInstance().addJob(new HBCIDauerauftragListJob(this.konto));
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
  String getIdentifier() {
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
    konto.addToProtokoll(i18n.tr("Dauerauftrag gelöscht an {0}",dauerauftrag.getGegenkontoName()),Protokoll.TYP_SUCCESS);
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


/**********************************************************************
 * $Log: HBCIDauerauftragDeleteJob.java,v $
 * Revision 1.22  2012/03/01 22:19:15  willuhn
 * @N i18n statisch und expliziten Super-Konstruktor entfernt - unnoetig
 *
 * Revision 1.21  2011-09-12 11:53:25  willuhn
 * @N Support fuer Banken (wie die deutsche Bank), die keine Order-IDs vergeben - BUGZILLA 1129
 *
 * Revision 1.20  2008-11-17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.19  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.18  2007/12/13 14:20:00  willuhn
 * @B Bug 517
 *
 * Revision 1.17  2007/12/06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.16  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.15  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/