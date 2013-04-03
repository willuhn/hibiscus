/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/HBCISynchronizeJobProviderKontoauszug.java,v $
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
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszug;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer das Abrufen von Saldo und/oder Kontoauszug.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderKontoauszug extends AbstractHBCISynchronizeJobProvider
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

        // Weder Saldo- noch Kontoauszug aktiv.
        // Also nichts zu tun.
        if (!options.getSyncKontoauszuege() && !options.getSyncSaldo())
          continue;
        
        SynchronizeJobKontoauszug job = backend.create(SynchronizeJobKontoauszug.class,kt);
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
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Umsaetze und Salden werden zum Schluss ausgefuehrt,
    // damit die oben gesendeten Ueberweisungen gleich mit
    // erscheinen, insofern die Bank das unterstuetzt.
    return 1;
  }
}
