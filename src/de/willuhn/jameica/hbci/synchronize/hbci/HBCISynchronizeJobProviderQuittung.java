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
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;

/**
 * Implementierung eines Job-Providers fuer das Senden von Empfangsquittungen.
 */
@Lifecycle(Type.CONTEXT)
public class HBCISynchronizeJobProviderQuittung extends AbstractHBCISynchronizeJobProvider
{
  @Resource
  private HBCISynchronizeBackend backend = null;

  private final static List<Class<? extends SynchronizeJob>> JOBS = new ArrayList<Class<? extends SynchronizeJob>>()
  {{
    add(HBCISynchronizeJobQuittung.class);
  }};
  
  @Override
  public List<SynchronizeJob> getSynchronizeJobs(Konto k)
  {
    // Nie per Synchronisation
    return Collections.emptyList();
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
    return 10; // ganz am Ende
  }
}
