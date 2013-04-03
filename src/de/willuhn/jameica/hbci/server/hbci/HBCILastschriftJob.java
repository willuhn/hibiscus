/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCILastschriftJob.java,v $
 * $Revision: 1.25 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Lastschrift".
 */
public class HBCILastschriftJob extends AbstractHBCIJob
{
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
			empfaenger.setBlz(lastschrift.getGegenkontoBLZ());
			empfaenger.setKontonummer(lastschrift.getGegenkontoNummer());
			empfaenger.setName(lastschrift.getGegenkontoName());

			setJobParam("other",Converter.Address2HBCIKonto(empfaenger));
			setJobParam("name",empfaenger.getName());
      setJobParamUsage(lastschrift);

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
  public String getIdentifier() {
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
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    lastschrift.setAusgefuehrt(true);

    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(lastschrift)); // BUGZILLA 480
    konto.addToProtokoll(i18n.tr("Lastschrift eingezogen von {0}",lastschrift.getGegenkontoName()),Protokoll.TYP_SUCCESS);
    Logger.info("lastschrift submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Ausführen der Lastschrift von {0}: {1}",new String[]{lastschrift.getGegenkontoName(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Ausführung der Lastschrift {0} abgebrochen",lastschrift.getGegenkontoName());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }
  
}
