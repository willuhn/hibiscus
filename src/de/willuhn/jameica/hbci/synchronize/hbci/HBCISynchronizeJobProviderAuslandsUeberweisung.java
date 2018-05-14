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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelUeberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaUeberweisung;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer Auslandsueberweisungen.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderAuslandsUeberweisung extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobSepaUeberweisung.class);
    add(HBCISynchronizeJobSepaSammelUeberweisung.class);
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

        if (!options.getSyncAuslandsUeberweisungen())
          continue;
        
        // Einzelueberweisungen
        DBIterator list = kt.getAuslandsUeberweisungen();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          AuslandsUeberweisung u = (AuslandsUeberweisung) list.next();
          if (!u.ueberfaellig() || u.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSepaUeberweisung job = backend.create(SynchronizeJobSepaUeberweisung.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,u);
          jobs.add(job);
        }
        
        // Sammelueberweisungen
        list = k.getSepaSammelUeberweisungen();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          SepaSammelUeberweisung su = (SepaSammelUeberweisung) list.next();
          if (!su.ueberfaellig() || su.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSepaSammelUeberweisung job = backend.create(SynchronizeJobSepaSammelUeberweisung.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,su);
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
