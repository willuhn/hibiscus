/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCIDauerauftragStoreJob.java,v $
 * $Revision: 1.23 $
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

import org.kapott.hbci.GV_Result.GVRDauerEdit;
import org.kapott.hbci.GV_Result.GVRDauerNew;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.jameica.hbci.server.hbci.tests.TurnusRestriction;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Dauerauftrag bei der Bank speichern".
 * Der Job erkennt selbst, ob der Dauerauftrag bei der Bank neu angelegt werden
 * muss oder ob bereits einer vorliegt, der nur geaendert werden muss.
 */
public class HBCIDauerauftragStoreJob extends AbstractHBCIJob
{

	private Dauerauftrag dauerauftrag = null;
	private Konto konto 							= null;

	private boolean active = false;

  /**
	 * ct.
   * @param auftrag Dauerauftrag, der bei der Bank gespeichert werden soll
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIDauerauftragStoreJob(Dauerauftrag auftrag) throws ApplicationException, RemoteException
	{
    super();
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (auftrag == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Dauerauftrag aus"));

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
				setJobParam("orderid",auftrag.getOrderID());

			setJobParam("src",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
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


			// Jetzt noch die Tests fuer die Job-Restriktionen
			Properties p = HBCIFactory.getInstance().getJobRestrictions(this.konto,this);
			Enumeration keys = p.keys();
			while (keys.hasMoreElements())
			{
				String s = (String) keys.nextElement();
				Logger.info("[hbci job restriction] name: " + s + ", value: " + p.getProperty(s));
			}
			new TurnusRestriction(turnus,p).test();
			if (!active) // nur pruefen bei neuen Dauerauftraegen
				new PreTimeRestriction(dauerauftrag.getErsteZahlung(),p).test();
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
		if (active)
			return "DauerEdit";
  	return "DauerNew";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    String empfName = dauerauftrag.getGegenkontoName();
    if (active)
      return i18n.tr("Dauerauftrag an {0} aktualisieren",empfName);
    return i18n.tr("Dauerauftrag an {0}",empfName);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    String empfName = dauerauftrag.getGegenkontoName();

    if (dauerauftrag.isActive())
      konto.addToProtokoll(i18n.tr("Dauerauftrag aktualisiert an {0}",empfName),Protokoll.TYP_SUCCESS);
    else
      konto.addToProtokoll(i18n.tr("Dauerauftrag ausgeführt an {0} ",empfName),Protokoll.TYP_SUCCESS);

    // jetzt muessen wir noch die Order-ID speichern, wenn er neu eingereicht wurde
    String orderID = null;
    if (!active)
    {
      GVRDauerNew result = (GVRDauerNew) this.getJobResult();
      orderID = result.getOrderId();
    }
    else
    {
      GVRDauerEdit result = (GVRDauerEdit) this.getJobResult();
      orderID = result.getOrderId();
    }
    // Der Auftrag war neu, dann muessen wir noch die Order-ID speichern
    dauerauftrag.setOrderID(orderID);
    dauerauftrag.store();

    Logger.info("dauerauftrag submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Ausführen des Dauerauftrages an {0}: {1}",new String[]{dauerauftrag.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

}


/**********************************************************************
 * $Log: HBCIDauerauftragStoreJob.java,v $
 * Revision 1.23  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.22  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.21  2007/12/06 23:53:56  willuhn
 * @B Bug 490
 *
 * Revision 1.20  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.19  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 **********************************************************************/