/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;

/**
 * Container-Objekt fuer eine einzelne Synchronisierung.
 */
public class Synchronization
{
  private SynchronizeBackend backend = null;
  private List<SynchronizeJob> jobs = new ArrayList<SynchronizeJob>();
  
  /**
   * Speichert das Backend fuer die Synchronisierung.
   * @param backend das Backend.
   */
  public void setBackend(SynchronizeBackend backend)
  {
    this.backend = backend;
  }
  
  /**
   * Liefert das Backend fuer die Synchronisierung.
   * @return das Backend fuer die Synchronisierung.
   */
  public SynchronizeBackend getBackend()
  {
    return this.backend;
  }
  
  /**
   * Liefert die Jobs der Synchronisierung.
   * @return die Jobs der Synchronisierung.
   */
  public List<SynchronizeJob> getJobs()
  {
    return this.jobs;
  }
  
  /**
   * Speichert die Jobs der Synchronisierung.
   * @param jobs die Jobs der Synchronisierung.
   */
  public void setJobs(List<SynchronizeJob> jobs)
  {
    this.jobs = jobs;
  }
}


