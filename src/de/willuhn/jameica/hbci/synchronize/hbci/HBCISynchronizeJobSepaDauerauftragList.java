/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaDauerauftragListJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragList;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Abrufen der SEPA-Dauerauftraege eines Kontos.
 */
public class HBCISynchronizeJobSepaDauerauftragList extends SynchronizeJobSepaDauerauftragList implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCISepaDauerauftragListJob((Konto)this.getContext(CTX_ENTITY))};
  }
}
