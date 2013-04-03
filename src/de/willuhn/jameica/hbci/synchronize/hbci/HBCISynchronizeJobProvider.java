/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;


/**
 * Marker-Interface, um die eigenen Job-Provider zu finden.
 * Erweitert Comparable, um die Jobs sortieren zu koennen.
 */
public interface HBCISynchronizeJobProvider extends Comparable
{
  /**
   * Liefert eine Liste der auszufuehrenden Synchronisierungsjobs auf dem angegebenen Konto.
   * @param k das Konto.
   * Wenn kein Konto angegeben ist, werden die Jobs aller Konten zurueckgeliefert.
   * @return Liste der auszufuehrenden Jobs.
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k);
}


