/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer SEPA-Lastschriften.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderSepaLastschrift implements HBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobSepaLastschrift.class);
  }};

  /**
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    List<SynchronizeJob> jobs = new LinkedList<SynchronizeJob>();
    
    for (Konto kt:backend.getSynchronizeKonten(k))
    {
      try
      {
        final SynchronizeOptions options = new SynchronizeOptions(kt);

        if (!options.getSyncSepaLastschriften())
          continue;
        
        DBIterator list = kt.getSepaLastschriften();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          SepaLastschrift u = (SepaLastschrift) list.next();
          if (!u.ueberfaellig() || u.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSepaLastschrift job = backend.create(SynchronizeJobSepaLastschrift.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,u);
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
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#getJobTypes()
   */
  public List<Class<? extends SynchronizeJob>> getJobTypes()
  {
    return JOBS;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Nach Moeglichkeit zuerst
    return -1;
  }

}
