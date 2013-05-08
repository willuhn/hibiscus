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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragList;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobDauerauftragStore;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers zum Abrufen und Ausfuehren von Dauerauftraegen.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderDauerauftrag implements HBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobDauerauftragStore.class);
    add(HBCISynchronizeJobDauerauftragList.class);
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

        if (!options.getSyncDauerauftraege())
          continue;
        
        // Senden der neuen Dauerauftraege
        DBIterator list = kt.getDauerauftraege();
        while (list.hasNext())
        {
          Dauerauftrag d = (Dauerauftrag) list.next();
          if (d.isActive())
            continue; // Der wurde schon gesendet
          
          SynchronizeJobDauerauftragStore job = backend.create(SynchronizeJobDauerauftragStore.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,d);
          jobs.add(job);
        }
        
        // Abrufen der existierenden Dauerauftraege.
        SynchronizeJobDauerauftragList job = backend.create(SynchronizeJobDauerauftragList.class,kt);
        job.setContext(SynchronizeJob.CTX_ENTITY,kt);
        jobs.add(job);
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
