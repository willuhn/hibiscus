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

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;

/**
 * Marker-Interface, um die Job-Provider fuer das Backend zu finden.
 * Erweitert Comparable, um die Jobs sortieren zu koennen.
 */
public interface SynchronizeJobProvider extends Comparable
{
  /**
   * Liefert eine Liste der auszufuehrenden Synchronisierungsjobs auf dem angegebenen Konto.
   * @param k das Konto.
   * Wenn kein Konto angegeben ist, werden die Jobs aller Konten zurueckgeliefert.
   * @return Liste der auszufuehrenden Jobs.
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k);

  /**
   * Liefert eine Liste der implementierenden Klassen der Jobs, die
   * dieser Provider unterstuetzt.
   * @return Liste der implementierenden Klassen der Jobs des Providers.
   */
  public List<Class<? extends SynchronizeJob>> getJobTypes();

  /**
   * Prueft, ob der Job-Provider diesen Job fuer das angegebene Konto beherrscht.
   * @param type der Job-Typ.
   * @param k das konkrete Konto.
   * @return true, wenn er es beherrscht.
   */
  public boolean supports(Class<? extends SynchronizeJob> type, Konto k);
}
