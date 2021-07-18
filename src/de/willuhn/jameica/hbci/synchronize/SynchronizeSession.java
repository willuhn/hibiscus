/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
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
  private double progressWindow = 100d;
  private List<String> warnings = new ArrayList<String>();
  private List<String> errors = new ArrayList<String>();

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
   * Liefert die Anzahl der Prozentpunkte, innerhalb derer die aktuelle Job-Gruppe den Fortschritt erhoehen darf.
   * @return die Anzahl der Prozentpunkte.
   */
  public double getProgressWindow()
  {
    return progressWindow;
  }

  /**
   * Speichert die Anzahl der Prozentpunkte, innerhalb derer die aktuelle Job-Gruppe den Fortschritt erhoehen darf.
   * @param progressWindow die Anzahl der Prozentpunkte.
   */
  public void setProgressWindow(double progressWindow)
  {
    this.progressWindow = progressWindow;
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

  /**
   * Liefert die Liste der Warnungen.
   * @return warnings die Liste der Warnungen.
   */
  public List<String> getWarnings()
  {
    return warnings;
  }

  /**
   * Liefert die Liste der Fehlermeldungen.
   * @return errors die Liste der Fehlermeldungen.
   */
  public List<String> getErrors()
  {
    return errors;
  }
}
