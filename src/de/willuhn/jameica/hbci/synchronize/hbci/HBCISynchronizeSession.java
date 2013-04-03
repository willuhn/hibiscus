/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.JobGroup;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend.Worker;
import de.willuhn.util.ProgressMonitor;

/**
 * Enthaelt Zustandsinformationen ueber die ggf aktuell laufende HBCI-Synchronisierung.
 */
public class HBCISynchronizeSession implements SynchronizeSession
{
  private Worker worker = null;
  private int status = ProgressMonitor.STATUS_NONE;
  
  /**
   * ct.
   * @param worker
   */
  HBCISynchronizeSession(Worker worker)
  {
    this.worker = worker;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeSession#getKonto()
   */
  public Konto getKonto()
  {
    JobGroup group = this.worker.currentJobGroup;
    return group != null ? group.konto : null;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeSession#getProgressMonitor()
   */
  public ProgressMonitor getProgressMonitor()
  {
    return this.worker.monitor;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeSession#getStatus()
   */
  public int getStatus()
  {
    return this.status;
  }
  
  /**
   * Setzt den aktuellen Status der Synchronisierung.
   * @param status der aktuelle Status der Synchronisierung.
   */
  void setStatus(int status)
  {
    this.status = status;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeSession#cancel()
   */
  public void cancel()
  {
    this.worker.interrupt();
  }
}


