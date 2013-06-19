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
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakter Basis-Job fuer Sammel-Transfers.
 */
public abstract class AbstractHBCISammelTransferJob extends AbstractHBCIJob
{

	private SammelTransfer transfer = null;
	private Konto konto = null;
	
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
    String msg = i18n.tr("Fehler beim Ausführen des Sammel-Auftrages {0}: {1}",new String[]{transfer.getBezeichnung(),error});
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markCancelled()
   */
  void markCancelled() throws RemoteException, ApplicationException
  {
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
    
    SammelTransferBuchung[] buchungen = null;
    for (HBCIRetVal val:warnings)
    {
      String code = val.code;
      if (code == null || code.length() != 4)
        continue; // Hu?
      if (code.equals("3210") || code.equals("3220") || code.equals("3260") || code.equals("3290"))
      {
        if (buchungen == null) // on-demand laden
          buchungen = this.transfer.getBuchungenAsArray();
        
        // Jepp, wir haben offensichtlich eine fehlgeschlagene Buchung
        haveWarnings = true;
        // BUGZILLA 899 Wir schreiben die Warnungn auch noch ins Konto-Protokoll und ins Log
        Logger.warn(val.toString());
        konto.addToProtokoll(i18n.tr("Einzelne Buchung eines Sammelauftrages nicht ausgeführt: {0}",val.toString()),Protokoll.TYP_ERROR);
        
        // Checken, ob wir die Nummer der Position haben
        String[] params = val.params;
        if (params != null && params.length > 0)
        {
          for (String param:params)
          {
            try
            {
              if (param != null && param.matches("[0-9]{1,4}"))
              {
                int i = Integer.parseInt(param);
                SammelTransferBuchung b = buchungen[i-1]; // die von der Bank gemeldete Position ist 1-basiert
                b.setWarnung(val.text);
                b.store();
              }
            }
            catch (Exception e)
            {
              Logger.write(Level.DEBUG,"unable to parse parameter \"" + param + "\" as position or no valid position in array",e);
            }
          }
        }
      }
    }

    // Ins Monitor-Fenster schreiben
    if (haveWarnings)
    {
      this.transfer.setWarning(true);
      this.transfer.store();
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      session.getProgressMonitor().log("    ** " + i18n.tr("Eine oder mehrere Buchungen des Sammelauftrages wurde nicht ausgeführt!"));
    }
  }

}
