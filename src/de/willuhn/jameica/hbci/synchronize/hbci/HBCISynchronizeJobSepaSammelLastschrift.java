/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaSammelLastschriftJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen SEPA-Sammellastschrift.
 */
public class HBCISynchronizeJobSepaSammelLastschrift extends SynchronizeJobSepaSammelLastschrift implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCISepaSammelLastschriftJob((SepaSammelLastschrift) this.getContext(CTX_ENTITY))};
  }

}
