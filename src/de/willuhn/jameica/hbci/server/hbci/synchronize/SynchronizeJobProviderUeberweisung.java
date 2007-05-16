/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeJobProviderUeberweisung.java,v $
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
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Implementierung eines Job-Providers fuer Ueberweisungen.
 */
public class SynchronizeJobProviderUeberweisung implements SynchronizeJobProvider
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    if (k == null)
      return null;
    
    final SynchronizeOptions options = new SynchronizeOptions(k);

    if (!options.getSyncUeberweisungen())
      return null;
    
    ArrayList jobs = new ArrayList();

    // Einzelueberweisungen
    DBIterator list = k.getUeberweisungen();
    list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
    while (list.hasNext())
    {
      Ueberweisung u = (Ueberweisung) list.next();
      if (!u.ueberfaellig() || u.ausgefuehrt()) // Doppelt haelt besser ;)
        continue; // Nur ueberfaellige Auftraege
      jobs.add(new SynchronizeUeberweisungJob(u));
    }

    // Sammelueberweisungen
    list = k.getSammelUeberweisungen();
    list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
    while (list.hasNext())
    {
      SammelUeberweisung su = (SammelUeberweisung) list.next();
      if (!su.ueberfaellig() || su.ausgefuehrt()) // Doppelt haelt besser ;)
        continue; // Nur ueberfaellige Auftraege
      jobs.add(new SynchronizeSammelUeberweisungJob(su));
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
 * $Log: SynchronizeJobProviderUeberweisung.java,v $
 * Revision 1.1  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 **********************************************************************/