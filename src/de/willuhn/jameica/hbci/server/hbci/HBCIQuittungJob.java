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

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.kapott.hbci.GV.GVReceipt;
import org.kapott.hbci.comm.Comm;

import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer das Senden einer Quittung an die Bank.
 */
public class HBCIQuittungJob extends AbstractHBCIJob
{
  private Kontoauszug ka = null;

  /**
	 * ct.
   * @param ka der Kontoauszug.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCIQuittungJob(Kontoauszug ka) throws ApplicationException, RemoteException
	{
		try
		{
			if (ka == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Kontoauszug aus"));
			
			this.ka = ka;

      byte[] receipt = this.ka.getQuittungscode();
      if (receipt == null || receipt.length == 0)
        throw new ApplicationException(i18n.tr("Kontoauszug enthält keinen Quittungscode")); 

			if (ka.isNewObject())
				ka.store();

			String s = null;
			try
			{
			  s = new String(receipt,Comm.ENCODING);
			}
			catch (UnsupportedEncodingException e)
			{
			  Logger.error("unable to encode receipt using encoding " + Comm.ENCODING + " - fallback to default encoding",e);
			}
			
			if (s == null)
			  s = new String(receipt);
			
      setJobParam("receipt",s);
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
    return this.ka;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getIdentifier()
   */
  public String getIdentifier()
  {
    return GVReceipt.getLowlevelName();
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#getName()
   */
  public String getName() throws RemoteException
  {
    int jahr   = this.getJahr();
    Integer nr = ka.getNummer();
    
    StringBuilder sb = new StringBuilder();
    sb.append(jahr);
    if (nr != null)
    {
      sb.append("-");
      sb.append(nr);
    }
    return i18n.tr("Empfangsquittung für Kontoauszug {0}",sb.toString());
  }
  
  /**
   * Versucht das Jahr des Kontoauszuges zu ermitteln.
   * @return das jahr des Kontoauszuges.
   */
  private int getJahr()
  {
    try
    {
      Integer jahr = ka.getJahr();
      if (jahr != null)
        return jahr;

      Calendar cal = Calendar.getInstance();

      List<Date> dates = Arrays.asList(ka.getVon(),ka.getErstellungsdatum(),ka.getAusfuehrungsdatum());
      for (Date d:dates)
      {
        if (d != null)
        {
          cal.setTime(d);
          return cal.get(Calendar.YEAR);
        }
      }
      
      // Ueberhaupt kein Jahr ermittelbar? Dann halt das aktuelle
      Logger.warn("unable to determine year for account statements");
      return cal.get(Calendar.YEAR);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine year for account statements",e);
      Calendar cal = Calendar.getInstance();
      return cal.get(Calendar.YEAR);
    }
      
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markExecuted()
   */
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    ka.setQuittiertAm(new Date());
    ka.store();
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ka));
    Logger.info("marked account statement for range " + ka.getVon() + " - " + ka.getBis() + " as received, receipt: " + ka.getQuittungscode());
  }
  
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob#markFailed(java.lang.String)
   */
  protected String markFailed(String error) throws RemoteException, ApplicationException
  {
    String msg = i18n.tr("Fehler beim Quittieren der elektronischen Kontoauszüge: {0}",error);
    ka.getKonto().addToProtokoll(msg,Protokoll.TYP_ERROR);
    return msg;
  }
}
