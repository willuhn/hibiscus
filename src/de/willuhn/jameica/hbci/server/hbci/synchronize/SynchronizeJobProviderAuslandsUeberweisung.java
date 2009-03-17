/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeJobProviderAuslandsUeberweisung.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/17 23:44:15 $
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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider;

/**
 * Implementierung eines Job-Providers fuer Auslandsueberweisungen.
 */
public class SynchronizeJobProviderAuslandsUeberweisung implements SynchronizeJobProvider
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider#getSynchronizeJobs(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    if (k == null)
      return null;
    
    final SynchronizeOptions options = new SynchronizeOptions(k);

    if (!options.getSyncAuslandsUeberweisungen())
      return null;
    
    ArrayList jobs = new ArrayList();

    DBIterator list = k.getAuslandsUeberweisungen();
    list.addFilter("(ausgefuehrt is null or ausgefuehrt = 0)"); // Schnelleres Laden durch vorheriges Aussortieren
    while (list.hasNext())
    {
      AuslandsUeberweisung u = (AuslandsUeberweisung) list.next();
      if (!u.ueberfaellig() || u.ausgefuehrt()) // Doppelt haelt besser ;)
        continue; // Nur ueberfaellige Auftraege
      jobs.add(new SynchronizeAuslandsUeberweisungJob(u));
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
 * $Log: SynchronizeJobProviderAuslandsUeberweisung.java,v $
 * Revision 1.1  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 **********************************************************************/