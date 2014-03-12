/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISepaSammelUeberweisungJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Ein Synchronize-Job fuer das Ausfuehren einer faelligen SEPA-Sammelueberweisung.
 */
public class HBCISynchronizeJobSepaSammelUeberweisung extends SynchronizeJobSepaSammelUeberweisung implements HBCISynchronizeJob
{
  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJob#createHBCIJobs()
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException
  {
    return new AbstractHBCIJob[]{new HBCISepaSammelUeberweisungJob((SepaSammelUeberweisung) this.getContext(CTX_ENTITY))};
  }

}
