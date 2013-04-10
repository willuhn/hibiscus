/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.JobGroup;
import de.willuhn.jameica.hbci.synchronize.AbstractSynchronizeBackend.Worker;
import de.willuhn.util.ProgressMonitor;

/**
 * Enthaelt Zustandsinformationen ueber die ggf aktuell laufende HBCI-Synchronisierung.
 */
public class SynchronizeSession
{
  private Worker worker = null;
  private int status = ProgressMonitor.STATUS_NONE;
  
  /**
   * ct.
   * @param worker
   */
  SynchronizeSession(Worker worker)
  {
    this.worker = worker;
  }
  
  /**
   * Liefert das aktuelle Konto.
   * @return konto das aktuelle Konto.
   */
  public Konto getKonto()
  {
    JobGroup group = this.worker.getCurrentJobGroup();
    return group != null ? group.getKonto() : null;
  }
  
  /**
   * Liefert den Progress-Monitor.
   * @return monitor der Progress-Monitor.
   */
  public ProgressMonitor getProgressMonitor()
  {
    return this.worker.getMonitor();
  }
  
  /**
   * Liefert den aktuellen Status der Synchronisierung.
   * @return der aktuelle Status der Synchronisierung.
   * @see ProgressMonitor#STATUS_NONE
   * @see ProgressMonitor#STATUS_RUNNING
   * @see ProgressMonitor#STATUS_CANCEL
   * @see ProgressMonitor#STATUS_ERROR
   * @see ProgressMonitor#STATUS_DONE
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
   * Bricht die Synchronisierung ab.
   */
  public void cancel()
  {
    this.worker.interrupt();
  }
}


