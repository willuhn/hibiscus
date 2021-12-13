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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Sammellastschrift".
 */
public class HBCISepaSammelLastschriftJob extends AbstractHBCISepaSammelTransferJob<SepaSammelLastschrift>
{
  private SepaLastType type = null;

  /**
	 * ct.
   * @param lastschrift die auszufuehrende Sammel-Lastschrift.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaSammelLastschriftJob(SepaSammelLastschrift lastschrift) throws ApplicationException, RemoteException
	{
    super(lastschrift);
    
    this.type = lastschrift.getType();

    List<SepaSammelLastBuchung> buchungen = lastschrift.getBuchungen();
    
    for (int i=0;i<buchungen.size();++i)
    {
      SepaSammelLastBuchung b = buchungen.get(i);
      
      // Wir nehmen explizit ein Integer-Objekt, um sicherzugehen, dass
      // wir nicht durch Autoboxing die falsche Signatur erwischen
      Integer idx = Integer.valueOf(i);
      
      setJobParam("mandateid",     idx, b.getMandateId());
      setJobParam("manddateofsig", idx, b.getSignatureDate());
      setJobParam("creditorid",    idx, b.getCreditorId());
      
      String purp = b.getPurposeCode();
      if (purp != null && purp.length() > 0)
        setJobParam("purposecode",idx, purp);
    }
    
    setJobParam("sequencetype",lastschrift.getSequenceType().name());
    if (this.type != null)
      setJobParam("type",this.type.name());
    
    Date targetDate = lastschrift.getTargetDate();
    if (targetDate != null)
      setJobParam("targetdate",targetDate);
	}

  @Override
  public String getIdentifier()
  {
    if (this.type != null)
      return this.type.getMultiJobName();
    
    // Default CORE
    return SepaLastType.DEFAULT.getMultiJobName();
  }

  @Override
  public String getName() throws RemoteException
  {
    return i18n.tr("SEPA-Sammellastschrift {0}",getSammelTransfer().getBezeichnung());
  }
  
  @Override
  protected void markExecuted() throws RemoteException, ApplicationException
  {
    super.markExecuted();
    
    // Wenn wir zugeordnete Adressen haben, koennen wir den Sequenz-Type umsetzen
    List<SepaSammelLastBuchung> buchungen = this.getSammelTransfer().getBuchungen();
    for (SepaSammelLastBuchung b:buchungen)
    {
      String id = StringUtils.trimToNull(MetaKey.ADDRESS_ID.get(b));
      if (id != null)
      {
        try
        {
          HibiscusAddress ha = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,id);
          String seqCode = StringUtils.trimToNull(MetaKey.SEPA_SEQUENCE_CODE.get(ha));
          if (seqCode != null)
          {
            SepaLastSequenceType type = SepaLastSequenceType.valueOf(seqCode);
            if (type == SepaLastSequenceType.FRST)
            {
              Logger.debug("auto-switching sequence-code for address-id" + id + " from FRST to RCUR");
              MetaKey.SEPA_SEQUENCE_CODE.set(ha,SepaLastSequenceType.RCUR.name());
            }
          }
        }
        catch (IllegalArgumentException ie)
        {
          Logger.error("unable to determine enum value of SepaLastSequenceType",ie);
        }
        catch (ObjectNotFoundException e)
        {
          Logger.info("address-id " + id + " no longer exists, unable to auto-switch sequence-code");
        }
        catch (RemoteException re)
        {
          Logger.error("unable to to auto-switch sequence-code for address-id" + id,re);
        }
      }
    }
  }

}
