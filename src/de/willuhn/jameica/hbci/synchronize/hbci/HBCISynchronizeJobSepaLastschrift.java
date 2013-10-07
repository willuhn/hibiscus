/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaLastschriftJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen SEPA-Lastschrift.
 */
public class HBCISynchronizeJobSepaLastschrift extends SynchronizeJobSepaLastschrift implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCISepaLastschriftJob((SepaLastschrift) this.getContext(CTX_ENTITY))};
  }

}
