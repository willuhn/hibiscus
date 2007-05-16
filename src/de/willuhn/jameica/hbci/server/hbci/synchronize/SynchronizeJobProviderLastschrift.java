/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeJobProviderLastschrift.java,v $
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

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider;

/**
 * Implementierung eines Job-Providers fuer Lastschriften.
 */
public class SynchronizeJobProviderLastschrift implements SynchronizeJobProvider
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    if (k == null)
      return null;
    
    final SynchronizeOptions options = new SynchronizeOptions(k);

    if (!options.getSyncLastschriften())
      return null;
    
    ArrayList jobs = new ArrayList();

    // Einzellastschriften
    DBIterator list = k.getLastschriften();
    list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
    while (list.hasNext())
    {
      Lastschrift l = (Lastschrift) list.next();
      if (!l.ueberfaellig() || l.ausgefuehrt()) // Doppelt haelt besser ;)
        continue; // Nur ueberfaellige Auftraege
      jobs.add(new SynchronizeLastschriftJob(l));
    }

    // Sammellastschriften
    list = k.getSammelLastschriften();
    list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
    while (list.hasNext())
    {
      SammelLastschrift sl = (SammelLastschrift) list.next();
      if (!sl.ueberfaellig() || sl.ausgefuehrt()) // Doppelt haelt besser ;)
        continue; // Nur ueberfaellige Auftraege
      jobs.add(new SynchronizeSammelLastschriftJob(sl));
    }
    return PseudoIterator.fromArray((SynchronizeJob[])jobs.toArray(new SynchronizeJob[jobs.size()]));
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


/*********************************************************************
 * $Log: SynchronizeJobProviderLastschrift.java,v $
 * Revision 1.1  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 **********************************************************************/