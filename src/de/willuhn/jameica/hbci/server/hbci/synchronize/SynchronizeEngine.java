/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/synchronize/SynchronizeEngine.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/01/26 23:17:46 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.synchronize;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.SynchronizeJobProvider;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Diese Engine ermittelt die eine Liste von HBCI-Synchronisierungs-Jobs.
 * Die Klasse implementiert selbst ebenfalls das Job-Provider-Interface -
 * quasi als Proxy fuer die konkreten Job-Provider.
 */
public class SynchronizeEngine implements SynchronizeJobProvider
{
  private static SynchronizeEngine engine = null;
    private ArrayList providers = null;

  private SynchronizeEngine()
  {
    Logger.info("init synchronize engine");
  }

  /**
   * Liefert die Instanz der Synchronize-Engine.
   * @return Instanz.
   */
  public synchronized static SynchronizeEngine getInstance()
  {
    if (engine != null)
      return engine;
    
    engine = new SynchronizeEngine();
    engine.providers = new ArrayList();
    
    try
    {
      Logger.info("loading hbci synchronize providers");
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(SynchronizeJobProvider.class);
      for (int i=0;i<found.length;++i)
      {
        if (found[i].getName().equals(SynchronizeEngine.class.getName()))
          continue; // Das sind wir selbst. Wuerde sonst eine Rekursion ausloesen.
        try
        {
          engine.providers.add((SynchronizeJobProvider) found[i].newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load synchronize provider " + found[i].getName() + ", skipping",e);
        }
      }
      
      // Sortieren der Jobs
      Collections.sort(engine.providers);
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no synchronize providers found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching vor synchronize providers",e);
    }
    return engine;
  }

  /**
   * Liefert eine Liste der verfuegbaren Synchronize-Jobs aller Konten.
   * @return Liste der gefundenen Jobs.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs() throws RemoteException
  {
    ArrayList all = new ArrayList();
    
    GenericIterator konten = getSynchronizeKonten();
    while (konten.hasNext())
    {
      GenericIterator jobs = getSynchronizeJobs((Konto) konten.next());
      all.addAll(PseudoIterator.asList(jobs));
    }
    return PseudoIterator.fromArray((SynchronizeJob[])all.toArray(new SynchronizeJob[all.size()]));
  }

  /**
   * Liefert eine Liste der verfuegbaren Synchronize-Jobs des angegebenen
   * @param k das Konto..
   * @return Liste der gefundenen Jobs.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    // Ohne Konto liefern wir nur eine leere Liste.
    if (k == null)
      return PseudoIterator.fromArray(new GenericObject[0]);
    
    ArrayList jobs = new ArrayList();
    
    for (int i=0;i<providers.size();++i)
    {
      SynchronizeJobProvider provider = (SynchronizeJobProvider) providers.get(i);
      GenericIterator list = provider.getSynchronizeJobs(k);
      if (list == null || list.size() == 0)
        continue;
      jobs.addAll(PseudoIterator.asList(list));
    }

    return PseudoIterator.fromArray((SynchronizeJob[]) jobs.toArray(new SynchronizeJob[jobs.size()]));
  }
  
  /**
   * Liefert die Liste der zu synchronisierenden Konten.
   * @return Liste der zu synchronisierenden Konten.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeKonten() throws RemoteException
  {
    DBIterator konten = Settings.getDBService().createList(Konto.class);
    konten.setOrder("order by id"); // Konten-Sortierung immer gleich
    ArrayList l = new ArrayList();
    while (konten.hasNext())
    {
      Konto k = (Konto) konten.next();
      SynchronizeOptions o = new SynchronizeOptions(k);
      if (o.getSynchronize())
        l.add(k);
    }
    return PseudoIterator.fromArray((Konto[])l.toArray(new Konto[l.size()]));
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    // Egal.
    return 0;
  }
}


/**********************************************************************
 * $Log: SynchronizeEngine.java,v $
 * Revision 1.2  2009/01/26 23:17:46  willuhn
 * @R Feld "synchronize" aus Konto-Tabelle entfernt. Aufgrund der Synchronize-Optionen pro Konto ist die Information redundant und ergibt sich implizit, wenn fuer ein Konto irgendeine der Synchronisations-Optionen aktiviert ist
 *
 * Revision 1.1  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 * Revision 1.9  2007/03/23 00:11:51  willuhn
 * @N Bug 346
 *
 * Revision 1.8  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.7  2006/08/28 21:28:26  willuhn
 * @B bug 277
 *
 * Revision 1.6  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 * Revision 1.5  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/03/21 00:44:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 * Revision 1.2  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 * Revision 1.1  2006/03/16 18:23:36  willuhn
 * @N first code for new synchronize system
 *
 **********************************************************************/