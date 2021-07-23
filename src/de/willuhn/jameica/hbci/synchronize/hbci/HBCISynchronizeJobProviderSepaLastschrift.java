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
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaLastschrift;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelLastschrift;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer SEPA-Lastschriften.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderSepaLastschrift extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobSepaLastschrift.class);
    add(HBCISynchronizeJobSepaSammelLastschrift.class);
  }};

  @Override
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
        
        // Einzellastschriften
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
        
        // Sammellastschriften
        list = k.getSepaSammelLastschriften();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          SepaSammelLastschrift sl = (SepaSammelLastschrift) list.next();
          if (!sl.ueberfaellig() || sl.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSepaSammelLastschrift job = backend.create(SynchronizeJobSepaSammelLastschrift.class,kt);
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

  @Override
  public boolean supports(Class<? extends SynchronizeJob> type, Konto k)
  {
    return true;
  }

  @Override
  public List<Class<? extends SynchronizeJob>> getJobTypes()
  {
    return JOBS;
  }

  @Override
  public int compareTo(Object o)
  {
    // Nach Moeglichkeit zuerst
    return -1;
  }

}
