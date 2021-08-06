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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kapott.hbci.GV.GVKontoauszug;
import org.kapott.hbci.GV.GVKontoauszugPdf;
import org.kapott.hbci.GV_Result.GVRKontoauszug;
import org.kapott.hbci.GV_Result.GVRKontoauszug.Format;
import org.kapott.hbci.GV_Result.GVRKontoauszug.GVRKontoauszugEntry;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Job fuer den Abruf der Kontoauszuege im PDF-Format.
 */
public class HBCIKontoauszugJob extends AbstractHBCIJob
{
	private Konto konto = null;
	
	private List<AbstractHBCIJob> followers = new ArrayList<AbstractHBCIJob>();

  /**
	 * ct.
   * @param konto Konto, fuer das die Kontoauszuege abgerufen werden sollen.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIKontoauszugJob(Konto konto) throws ApplicationException, RemoteException
	{
		try
		{
			if (konto == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus")); 

			if (konto.isNewObject())
				konto.store();

			this.konto = konto;
			
      String curr = konto.getWaehrung();
      if (curr == null || curr.length() == 0)
        konto.setWaehrung(HBCIProperties.CURRENCY_DEFAULT_DE);
			setJobParam("my",Converter.HibiscusKonto2HBCIKonto(konto));
			
			// Format bei Bedarf mitschicken - nur bei "HKEKA" noetig ("HKEKP" ist eh immer PDF)
			if (this.getIdentifier().equals(GVKontoauszug.getLowlevelName()))
			{
			  // Aber erstmal checken, ob die Bank ueberhaupt PDF unterstuetzt.
			  // Denn wenn nicht, duerfen nicht "PDF" als Format hinschicken
			  Support support = BPDUtil.getSupport(this.konto,BPDUtil.Query.Kontoauszug);
			  if (support != null)
			  {
	        List<Format> formats = KontoauszugPdfUtil.getFormats(support.getBpd());

	        // Wenn die Bank PDF nicht unterstuetzt, lassen wir den Parameter einfach weg
	        if (formats.contains(Format.PDF))
	          setJobParam("format",GVRKontoauszug.Format.PDF.getCode());
			  }
			      
			}
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
    return this.konto;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    // Ist abhaengig davon, welche Job-Variante fuer das Konto unterstuetzt werden:
    
    Support support = BPDUtil.getSupport(this.konto,BPDUtil.Query.KontoauszugPdf);
    if (support != null && support.isSupported())
      return GVKontoauszugPdf.getLowlevelName();
    
    return GVKontoauszug.getLowlevelName();
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Elektronische Kontoauszüge {0}",konto.getLongName());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    GVRKontoauszug result = (GVRKontoauszug) getJobResult();
    List<GVRKontoauszugEntry> entries = result.getEntries();
    
    // Datum des letzten Abrufs in den Meta-Daten speichern - auch dann, wenn keine neuen vorlagen
    MetaKey.KONTOAUSZUG_INTERVAL_LAST.set(this.konto,HBCI.LONGDATEFORMAT.format(new Date()));
    
    if (entries != null && entries.size() > 0)
    {
      for (GVRKontoauszugEntry entry:entries)
      {
        try
        {
          byte[] data = entry.getData();
          if (data == null || data.length == 0)
          {
            Logger.info("no new account statements");
            
            BeanService service = Application.getBootLoader().getBootable(BeanService.class);
            SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();

            ProgressMonitor monitor = session.getProgressMonitor();
            monitor.log(i18n.tr("Keine neuen Kontoauszüge verfügbar"));

            return;
          }
          
          Kontoauszug ka = Converter.HBCIKontoauszug2HibiscusKontoauszug(this.konto,entry);
          
          if (Boolean.parseBoolean(MetaKey.KONTOAUSZUG_MARK_READ.get(this.konto)))
            ka.setGelesenAm(new Date());
          
          Logger.info("received new account statement for range " + ka.getVon() + " - " + ka.getBis());
          ka.store();
          KontoauszugPdfUtil.receive(ka,entry.getData());
          
          Application.getMessagingFactory().sendMessage(new ImportMessage(ka));
          konto.addToProtokoll(i18n.tr("Elektronische Kontoauszüge abgerufen"),Protokoll.TYP_SUCCESS);
          
          // Wenn wir eine Quittung haben, senden wir sie an die Bank, um den Empfang zu bestaetigen.
          // Aber nur, wenn das auch aktiviert ist
          boolean sendReceipt = Boolean.parseBoolean(MetaKey.KONTOAUSZUG_SEND_RECEIPT.get(this.konto));
          if (sendReceipt)
          {
            byte[] q = ka.getQuittungscode();
            if (q != null && q.length > 0)
              this.followers.add(new HBCIQuittungJob(ka));
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to store account statements, skipping",e);
          this.konto.addToProtokoll(i18n.tr("Speichern des elektronischen Kontoauszuges fehlgeschlagen: {0}",e.getMessage()),Protokoll.TYP_ERROR);
        }
      }
    }

  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getFollowerJobs()
   */
  @Override
  public List<AbstractHBCIJob> getFollowerJobs() throws RemoteException, ApplicationException
  {
    return this.followers;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Abrufen der elektronischen Kontoauszüge: {0}",error);
    konto.addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
