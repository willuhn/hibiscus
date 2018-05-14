/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragList;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaDauerauftragStore;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers zum Abrufen und Ausfuehren von SEPA-Dauerauftraegen.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderSepaDauerauftrag extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobSepaDauerauftragDelete.class);
    add(HBCISynchronizeJobSepaDauerauftragStore.class);
    add(HBCISynchronizeJobSepaDauerauftragList.class);
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

        if (!options.getSyncSepaDauerauftraege())
          continue;
        
        // Senden der neuen Dauerauftraege
        DBIterator list = kt.getSepaDauerauftraege();
        while (list.hasNext())
        {
          SepaDauerauftrag d = (SepaDauerauftrag) list.next();
          if (d.isActive())
            continue; // Der wurde schon gesendet
          
          SynchronizeJobSepaDauerauftragStore job = backend.create(SynchronizeJobSepaDauerauftragStore.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,d);
          jobs.add(job);
        }
        
        // Abrufen der existierenden Dauerauftraege.
        SynchronizeJobSepaDauerauftragList job = backend.create(SynchronizeJobSepaDauerauftragList.class,kt);
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
   * @see de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider#supports(java.lang.Class, de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public boolean supports(Class<? extends SynchronizeJob> type, Konto k)
  {
    return true;
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
