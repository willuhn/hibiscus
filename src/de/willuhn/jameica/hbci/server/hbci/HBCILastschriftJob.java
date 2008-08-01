/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCILastschriftJob.java,v $
 * $Revision: 1.16 $
 * $Date: 2008/08/01 11:05:14 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Job fuer "Lastschrift".
 */
public class HBCILastschriftJob extends AbstractHBCIJob
{

	private I18N i18n = null;
	private Lastschrift lastschrift = null;
	private Konto konto = null;

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCILastschriftJob(Lastschrift lastschrift) throws ApplicationException, RemoteException
	{
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (lastschrift == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Lastschrift an"));
		
			if (lastschrift.isNewObject())
				lastschrift.store();

      if (lastschrift.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Lastschrift wurde bereits ausgeführt"));

			this.lastschrift = lastschrift;
			this.konto = lastschrift.getKonto();

      if (this.lastschrift.getBetrag() > Settings.getUeberweisungLimit())
        throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
          HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));

      setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));

      // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

			setJobParam("btg",lastschrift.getBetrag(),curr);

      String key = lastschrift.getTextSchluessel();
      if (key != null && key.length() > 0)
        setJobParam("type",key);

			HibiscusAddress empfaenger = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
			empfaenger.setBLZ(lastschrift.getGegenkontoBLZ());
			empfaenger.setKontonummer(lastschrift.getGegenkontoNummer());
			empfaenger.setName(lastschrift.getGegenkontoName());

			setJobParam("other",Converter.Address2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());

			setJobParam("usage",lastschrift.getZweck());

			String zweck2 = lastschrift.getZweck2();
			if (zweck2 != null && zweck2.length() > 0)
				setJobParam("usage_2",zweck2);
      
      GenericIterator moreUsages = lastschrift.getWeitereVerwendungszwecke();
      int pos = 3;
      while (moreUsages != null && moreUsages.hasNext())
      {
        Verwendungszweck zweck = (Verwendungszweck) moreUsages.next();
        String text = zweck.getText();
        if (text == null || text.length() == 0)
          continue;
        setJobParam("usage_" + pos,text);
        pos++;
      }
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
    return "Last";
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Lastschrift an {0}",lastschrift.getGegenkontoName());
  }

  /**
   * Prueft, ob die lastschrift erfolgreich war und markiert diese im Erfolgsfall als "ausgefuehrt".
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#handleResult()
   */
  void handleResult() throws ApplicationException, RemoteException
  {
		String empfName = lastschrift.getGegenkontoName();

		if (getJobResult().isOK())
		{
      // Wir markieren die Ueberweisung als "ausgefuehrt"
      lastschrift.setAusgefuehrt();
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(lastschrift)); // BUGZILLA 480
      konto.addToProtokoll(i18n.tr("Lastschrift eingezogen von {0}",empfName),Protokoll.TYP_SUCCESS);
      Logger.info("lastschrift submitted successfully");
      return;
		}
    String msg = i18n.tr("Fehler beim Ausführen der Lastschrift von {0}",empfName);
    String error = getStatusText();
    konto.addToProtokoll(msg + ": " + error,Protokoll.TYP_ERROR);
    throw new ApplicationException(msg + ": " + error);
  }
}


/**********************************************************************
 * $Log: HBCILastschriftJob.java,v $
 * Revision 1.16  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.15  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.14  2007/12/06 14:42:26  willuhn
 * @B Bug 480
 *
 * Revision 1.13  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.12  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.11  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.10  2006/06/19 11:52:15  willuhn
 * @N Update auf hbci4java 2.5.0rc9
 *
 * Revision 1.9  2006/04/25 16:39:06  willuhn
 * @N Konstruktoren von HBCI-Jobs werfen nun eine ApplicationException, wenn der Auftrag bereits ausgefuehrt wurde
 *
 * Revision 1.8  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.7  2006/03/15 17:28:41  willuhn
 * @C Refactoring der Anzeige der HBCI-Fehlermeldungen
 *
 * Revision 1.6  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.2  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/