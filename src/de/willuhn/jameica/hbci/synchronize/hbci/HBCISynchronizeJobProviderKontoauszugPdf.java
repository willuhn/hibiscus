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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.KontoauszugInterval;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobKontoauszugPdf;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Job-Providers fuer das Abrufen der Kontoauszuege im PDF-Format.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderKontoauszugPdf extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobKontoauszugPdf.class);
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

        if (!options.getSyncKontoauszuegePdf())
          continue;
        
        // Checken, ob das Konto diesen Job unterstuetzt
        if (!KontoauszugPdfUtil.supported(kt))
          continue;

        // Jetzt noch checken, welche Synchronisierungseinstellungen fuer die
        // Kontoauszuege bei diesem Konto definiert sind.
        Date next = KontoauszugInterval.getNextInterval(k);
        if (next == null || next.after(new Date()))
          continue; // Kein Abruf faellig fuer dieses Konto.
        
        SynchronizeJobKontoauszugPdf job = backend.create(SynchronizeJobKontoauszugPdf.class,kt);
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
  
  @Override
  public boolean supports(Class<? extends SynchronizeJob> type, Konto k)
  {
    // Kein Konto angegeben. Dann gehen wir mal davon aus, dass es geht
    if (k == null)
      return true;
    
    return KontoauszugPdfUtil.supported(k);
  }

  @Override
  public List<Class<? extends SynchronizeJob>> getJobTypes()
  {
    return JOBS;
  }

  @Override
  public int compareTo(Object o)
  {
    return 10; // ganz am Ende
  }
}
