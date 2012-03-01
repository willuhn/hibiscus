/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/AbstractHBCISammelTransferJob.java,v $
 * $Revision: 1.15 $
 * $Date: 2012/03/01 22:25:29 $
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

import org.kapott.hbci.status.HBCIRetVal;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Basis-Job fuer Sammel-Transfers.
 */
public abstract class AbstractHBCISammelTransferJob extends AbstractHBCIJob
{

	private SammelTransfer transfer = null;
	private Konto konto = null;
	
	private boolean markExecutedBefore = false;

  /**
	 * ct.
   * Achtung. Der Job-Parameter "data" fehlt noch und muss in den
   * abgeleiteten Klassen gesetzt werden.
   * @param transfer der auszufuehrende Sammel-Transfer.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public AbstractHBCISammelTransferJob(SammelTransfer transfer) throws ApplicationException, RemoteException
	{
		try
		{
			if (transfer == null)
				throw new ApplicationException(i18n.tr("Bitte geben Sie einen Sammel-Auftrag an"));
		
			if (transfer.isNewObject())
				transfer.store();

      if (transfer.ausgefuehrt())
        throw new ApplicationException(i18n.tr("Sammel-Auftrag wurde bereits ausgeführt"));

			this.transfer = transfer;
			this.konto = transfer.getKonto();

      DBIterator buchungen = this.transfer.getBuchungen();
      while (buchungen.hasNext())
      {
        SammelTransferBuchung b = (SammelTransferBuchung) buchungen.next();
        if (b.getBetrag() > Settings.getUeberweisungLimit())
          throw new ApplicationException(i18n.tr("Auftragslimit überschritten: {0} ", 
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + this.konto.getWaehrung()));
      }
      
      
			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));

      de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
      markExecutedBefore = settings.getBoolean("transfer.markexecuted.before",false);
      if (markExecutedBefore)
        transfer.setAusgefuehrt(true);
			
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
   * Liefert den Sammel-Transfer.
   * @return der Sammel-Transfer.
   */
  SammelTransfer getSammelTransfer()
  {
    return this.transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  void markExecuted() throws RemoteException, ApplicationException
  {
    // Wenn der Auftrag nicht vorher als ausgefuehrt markiert wurde, machen wir das jetzt
    if (!markExecutedBefore)
      transfer.setAusgefuehrt(true);

    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(transfer)); // BUGZILLA 545
    konto.addToProtokoll(i18n.tr("Sammel-Auftrag {0} ausgeführt",transfer.getBezeichnung()),Protokoll.TYP_SUCCESS);
    Logger.info("sammellastschrift submitted successfully");
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  String markFailed(String error) throws RemoteException, ApplicationException
  {
    // Wenn der Auftrag fehlerhaft war und schon als ausgefuehrt markiert wurde, machen
    // wir das jetzt wieder rurckgaengig
    if (markExecutedBefore)
      transfer.setAusgefuehrt(false);

    String msg = i18n.tr("Fehler beim Ausführen des Sammel-Auftrages {0}: {1}",new String[]{transfer.getBezeichnung(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
    // Wenn der Auftrag abgebrochen wurde und schon als ausgefuehrt markiert wurde, machen
    // wir das jetzt wieder rurckgaengig
    if (markExecutedBefore)
      transfer.setAusgefuehrt(false);
    
    String msg = i18n.tr("Ausführung des Sammel-Auftrages {0} abgebrochen",transfer.getBezeichnung());
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#hasWarnings(org.kapott.hbci.status.HBCIRetVal[])
   */
  void hasWarnings(HBCIRetVal[] warnings) throws RemoteException, ApplicationException
  {
    // BUGZILLA 899
    // Wir checken, ob eventuell eine der enthaltenen Buchungen nicht ausgefuehrt wurde
    boolean haveWarnings = false;
    for (HBCIRetVal val:warnings)
    {
      String code = val.code;
      if (code == null || code.length() != 4)
        continue; // Hu?
      if (code.equals("3210") || code.equals("3220") || code.equals("3260") || code.equals("3290"))
      {
        // Jepp, wir haben offensichtlich eine fehlgeschlagene Buchung
        haveWarnings = true;
        // BUGZILLA 899 Wir schreiben die Warnungn auch noch ins Konto-Protokoll und ins Log
        Logger.warn(val.toString());
        konto.addToProtokoll(i18n.tr("Einzelne Buchung eines Sammelauftrages nicht ausgeführt: {0}",val.toString()),Protokoll.TYP_ERROR);
      }
    }

    // Ins Monitor-Fenster schreiben
    if (haveWarnings)
    {
      HBCIFactory.getInstance().getProgressMonitor().log("    ** " + i18n.tr("Eine oder mehrere Buchungen des Sammelauftrages wurde nicht ausgeführt!"));
    }
  }

}


/**********************************************************************
 * $Log: AbstractHBCISammelTransferJob.java,v $
 * Revision 1.15  2012/03/01 22:25:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2012/03/01 22:25:24  willuhn
 * @N BUGZILLA 899
 *
 * Revision 1.13  2012/03/01 22:19:15  willuhn
 * @N i18n statisch und expliziten Super-Konstruktor entfernt - unnoetig
 *
 * Revision 1.12  2010-09-02 10:21:06  willuhn
 * @N BUGZILLA 899
 *
 * Revision 1.11  2009/06/29 09:00:23  willuhn
 * @B wenn das Feature "transfer.markexecuted.before" aktiv ist, wurden Auftraege auch dann als ausgefuehrt markiert, wenn sie abgebrochen wurden - die Methode markCancelled() war nicht ueberschrieben worden
 *
 * Revision 1.10  2009/02/18 10:48:41  willuhn
 * @N Neuer Schalter "transfer.markexecuted.before", um festlegen zu koennen, wann ein Auftrag als ausgefuehrt gilt (wenn die Quittung von der Bank vorliegt oder wenn der Auftrag erzeugt wurde)
 *
 * Revision 1.9  2008/09/23 11:24:27  willuhn
 * @C Auswertung der Job-Results umgestellt. Die Entscheidung, ob Fehler oder Erfolg findet nun nur noch an einer Stelle (in AbstractHBCIJob) statt. Ausserdem wird ein Job auch dann als erfolgreich erledigt markiert, wenn der globale Job-Status zwar fehlerhaft war, aber fuer den einzelnen Auftrag nicht zweifelsfrei ermittelt werden konnte, ob er erfolgreich war oder nicht. Es koennte unter Umstaenden sein, eine Ueberweisung faelschlicherweise als ausgefuehrt markiert (wenn globaler Status OK, aber Job-Status != ERROR). Das ist aber allemal besser, als sie doppelt auszufuehren.
 *
 * Revision 1.8  2008/02/04 18:56:45  willuhn
 * @B Bug 545
 *
 * Revision 1.7  2007/12/06 14:25:32  willuhn
 * @B Bug 494
 **********************************************************************/