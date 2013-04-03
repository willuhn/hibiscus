/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer einen einzelnen Synchronisierungs-Job via HBCI.
 * Dient auch als Marker-Interface, um Implementierungen der Synchronize-Jobs
 * zu finden.
 */
public interface HBCISynchronizeJob extends SynchronizeJob
{
  /**
   * Erzeugt einen oder mehrere HBCI-Jobs basierend auf dem SynchronizeJob.
   * @return der/die erzeugten HBCI-Jobs.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public AbstractHBCIJob[] createHBCIJobs() throws RemoteException, ApplicationException;
}
