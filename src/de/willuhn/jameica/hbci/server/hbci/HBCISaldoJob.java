/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/HBCISaldoJob.java,v $
 * $Revision: 1.29 $
 * $Date: 2008/09/23 11:24:26 $
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

import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.structures.Saldo;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "Salden-Abfrage".
 */
public class HBCISaldoJob extends AbstractHBCIJob {

	private Konto konto = null;

  /**
	 * ct.
   * @param konto konto, fuer das der Saldo ermittelt werden soll.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISaldoJob(Konto konto) throws ApplicationException, RemoteException
	{
    super();
		try
		{
			i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;

			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));

      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;
      setJobParam("my.curr",curr);
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
  String getIdentifier()
  {
    return "SaldoReq";
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Saldo-Abruf {0}",konto.getLongName());
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    GVRSaldoReq result = (GVRSaldoReq) getJobResult();
    konto.addToProtokoll(i18n.tr("Saldo abgerufen"),Protokoll.TYP_SUCCESS);

    // Jetzt speichern wir noch den neuen Saldo.
    Saldo saldo = result.getEntries()[0].ready;
    konto.setSaldo(saldo.value.getDoubleValue());

    konto.store();
    Application.getMessagingFactory().sendMessage(new SaldoMessage(konto));
    Logger.info("saldo fetched successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen das Saldos: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}


/**********************************************************************
 * $Log: HBCISaldoJob.java,v $
 * Revision 1.29  2008/09/23 11:24:26  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.28  2007/12/11 13:46:47  willuhn
 * @N Waehrung auch bei Saldo-Abfrage - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43618#43618
 *
 * Revision 1.27  2007/12/11 13:17:26  willuhn
 * @N Waehrung bei Umsatzabfrage - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43618#43618
 *
 * Revision 1.26  2007/12/11 12:23:26  willuhn
 * @N Bug 355
 *
 * Revision 1.25  2007/12/11 11:59:40  willuhn
 * @N Waehrung bei Saldo-Job mit uebertragen. Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43610#43610
 *
 * Revision 1.24  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 *
 * Revision 1.23  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 **********************************************************************/