/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobProviderUeberweisung.java,v $
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
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSammelUeberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobUeberweisung;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer Ueberweisungen.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderUeberweisung extends AbstractHBCISynchronizeJobProvider
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

        if (!options.getSyncUeberweisungen())
          continue;
        
        // Einzelueberweisungen
        DBIterator list = k.getUeberweisungen();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          Ueberweisung u = (Ueberweisung) list.next();
          if (!u.ueberfaellig() || u.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobUeberweisung job = backend.create(SynchronizeJobUeberweisung.class,kt);
          job.setContext(SynchronizeJob.CTX_ENTITY,u);
          jobs.add(job);
        }

        // Sammelueberweisungen
        list = k.getSammelUeberweisungen();
        list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
        while (list.hasNext())
        {
          SammelUeberweisung su = (SammelUeberweisung) list.next();
          if (!su.ueberfaellig() || su.ausgefuehrt()) // Doppelt haelt besser ;)
            continue; // Nur ueberfaellige Auftraege
          
          SynchronizeJobSammelUeberweisung job = backend.create(SynchronizeJobSammelUeberweisung.class,kt);
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
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Reihenfolge egal.
    return 0;
  }

}
