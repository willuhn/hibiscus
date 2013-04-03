/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.util.ProgressMonitor;

/**
 * Enthaelt Zustandsinformationen ueber die ggf aktuell laufende Synchronisierung.
 */
public interface SynchronizeSession
{
  /**
   * Liefert das aktuelle Konto.
   * @return konto das aktuelle Konto.
   */
  public Konto getKonto();
  
  /**
   * Liefert den Progress-Monitor.
   * @return monitor der Progress-Monitor.
   */
  public ProgressMonitor getProgressMonitor();
  
  /**
   * Liefert den aktuellen Status der Synchronisierung.
   * @return der aktuelle Status der Synchronisierung.
   * @see ProgressMonitor#STATUS_NONE
   * @see ProgressMonitor#STATUS_RUNNING
   * @see ProgressMonitor#STATUS_CANCEL
   * @see ProgressMonitor#STATUS_ERROR
   * @see ProgressMonitor#STATUS_DONE
   */
  public int getStatus();
  
  /**
   * Bricht die Synchronisierung ab.
   */
  public void cancel();
}


