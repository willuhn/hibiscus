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
import java.util.Properties;

import org.kapott.hbci.GV.HBCIJob;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.server.hbci.tests.PreTimeRestriction;
import de.willuhn.util.ApplicationException;

/**
 * Job fuer "SEPA-Sammelueberweisung".
 */
public class HBCISepaSammelUeberweisungJob extends AbstractHBCISepaSammelTransferJob<SepaSammelUeberweisung>
{
  private boolean isTermin = false;

  /**
	 * ct.
   * @param u die auszufuehrende Sammel-Ueberweisung.
   * @throws ApplicationException
   * @throws RemoteException
   */
  public HBCISepaSammelUeberweisungJob(SepaSammelUeberweisung u) throws ApplicationException, RemoteException
	{
    super(u);
    this.isTermin = u.isTerminUeberweisung();
	}

  @Override
  public String getIdentifier()
  {
    if (this.isTermin)
      return "TermMultiUebSEPA";
    return "MultiUebSEPA";
  }
  
  @Override
  public void setJob(HBCIJob job) throws RemoteException, ApplicationException
  {
    if (this.isTermin)
    {
      Date date = this.getSammelTransfer().getTermin();
      Properties p = job.getJobRestrictions();
      new PreTimeRestriction(date,p).test();
      this.setJobParam("date",date);
    }
    super.setJob(job);
  }

  @Override
  public String getName() throws RemoteException
  {
    if (this.isTermin)
      return i18n.tr("SEPA-Sammelterminüberweisung {0}",getSammelTransfer().getBezeichnung());
    return i18n.tr("SEPA-Sammelüberweisung {0}",getSammelTransfer().getBezeichnung());
  }
}
