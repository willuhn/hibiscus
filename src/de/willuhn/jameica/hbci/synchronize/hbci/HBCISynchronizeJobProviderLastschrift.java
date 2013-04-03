/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobProviderLastschrift.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/05/16 11:32:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSammelLastschrift;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer Lastschriften.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderLastschrift extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  /**
   * @see de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    List<SynchronizeJob> jobs = new LinkedList<SynchronizeJob>();
    
    for (Konto kt:this.getKonten(k))
    {
      try
      {
        final SynchronizeOptions options = new SynchronizeOptions(kt);

        if (!options.getSyncLastschriften())
          continue;
        
        // Einzellastschriften
        DBIterator list = k.getLastschriften();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          Lastschrift l = (Lastschrift) list.next();
          if (!l.ueberfaellig() || l.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobLastschrift job = backend.create(SynchronizeJobLastschrift.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,l);
          jobs.add(job);
        }

        // Sammellastschriften
        list = k.getSammelLastschriften();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          SammelLastschrift sl = (SammelLastschrift) list.next();
          if (!sl.ueberfaellig() || sl.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSammelLastschrift job = backend.create(SynchronizeJobSammelLastschrift.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,sl);
          jobs.add(job);
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to load synchronize jobs",e);
      }
    }

    return jobs;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Reihenfolge egal.
    return 0;
  }
}
