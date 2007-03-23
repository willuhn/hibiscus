/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/SynchronizeEngine.java,v $
 * $Revision: 1.9 $
 * $Date: 2007/03/23 00:11:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeDauerauftragStoreJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeKontoauszugJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeLastschriftJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeSammelLastschriftJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeSammelUeberweisungJob;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeUeberweisungJob;
import de.willuhn.logging.Logger;

/**
 * Diese Engine ermittelt die eine Liste von HBCI-Synchronisierungs-Jobs.
 */
public class SynchronizeEngine
{
  private static SynchronizeEngine engine = null;

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
    if (engine == null)
      engine = new SynchronizeEngine();

    return engine;
  }

  /**
   * Liefert eine Liste der verfuegbaren Synchronize-Jobs aller Konten.
   * @return Liste der gefundenen Jobs.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs() throws RemoteException
  {
    return getSynchronizeJobs(null);
  }

  /**
   * Liefert eine Liste der verfuegbaren Synchronize-Jobs des angegebenen
   * @param k das Konto..
   * @return Liste der gefundenen Jobs.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs(Konto k) throws RemoteException
  {
    ArrayList jobs = new ArrayList();

    if (k != null)
    {
      findJobs(k,jobs);
    }
    else
    {
      GenericIterator konten = getSyncronizeKonten();
      while (konten.hasNext())
      {
        findJobs((Konto) konten.next(),jobs);
      }
    }
    return PseudoIterator.fromArray((SynchronizeJob[]) jobs.toArray(new SynchronizeJob[jobs.size()]));
  }
  
  /**
   * Liefert die Liste der zu synchronisierenden Konten.
   * @return Liste der zu synchronisierenden Konten.
   * @throws RemoteException
   */
  public GenericIterator getSyncronizeKonten() throws RemoteException
  {
    DBIterator konten = Settings.getDBService().createList(Konto.class);
    konten.addFilter("synchronize = 1 or synchronize is null"); // BUGZILLA 277
    return konten;
  }

  /**
   * Sucht nach allen Synchronisierungsjobs fuer dieses Konto und hangt sie an die Liste.
   * @param k Konto.
   * @param list Liste, an die die Jobs gehaengt werden.
   * @throws RemoteException
   */
  private void findJobs(Konto k, ArrayList list) throws RemoteException
  {
    Logger.info("adding open transfers");

    final SynchronizeOptions options = new SynchronizeOptions(k);

    if (options.getSyncUeberweisungen())
    {
      addTransfers(k.getUeberweisungen(),new TransferJobCreator() {
        public SynchronizeJob create(Terminable t)
        {
          return new SynchronizeUeberweisungJob((Ueberweisung)t);
        }
      },list);
      addTransfers(k.getSammelUeberweisungen(),new TransferJobCreator() {
        public SynchronizeJob create(Terminable t)
        {
          return new SynchronizeSammelUeberweisungJob((SammelUeberweisung)t);
        }
      },list);
    }


    if (options.getSyncLastschriften())
    {
      addTransfers(k.getLastschriften(),new TransferJobCreator() {
        public SynchronizeJob create(Terminable t)
        {
          return new SynchronizeLastschriftJob((Lastschrift)t);
        }
      },list);

      addTransfers(k.getSammelLastschriften(),new TransferJobCreator() {
        public SynchronizeJob create(Terminable t)
        {
          return new SynchronizeSammelLastschriftJob((SammelLastschrift)t);
        }
      },list);
    }

    if (options.getSyncDauerauftraege())
    {
      list.add(new SynchronizeDauerauftragListJob(k));
      DBIterator i = k.getDauerauftraege();
      while (i.hasNext())
      {
        Dauerauftrag u = (Dauerauftrag) i.next();
        if (u.isActive())
          continue;
        list.add(new SynchronizeDauerauftragStoreJob(u));
      }
    }


    if (options.getSyncKontoauszuege() || options.getSyncSaldo())
    {
      // Umsaetze und Salden werden zum Schluss ausgefuehrt,
      // damit die oben gesendeten Ueberweisungen gleich mit
      // erscheinen, insofern die Bank das unterstuetzt.
      Logger.info("adding kontoauszug/saldo job");
      list.add(new SynchronizeKontoauszugJob(k));
    }
  }
  
  private void addTransfers(GenericIterator transfers, TransferJobCreator creator, ArrayList list) throws RemoteException
  {
    while (transfers.hasNext())
    {
      Terminable t = (Terminable) transfers.next();
      if (t.ausgefuehrt())
        continue;
      if (t.ueberfaellig())
      {
        list.add(creator.create(t));
      }
    }
  }
  
  private interface TransferJobCreator
  {
    /**
     * Erzeugt einen neuen Synchronize-Job aus der Ueberweisung/Lastschrift.
     * @param t
     * @return der Job.
     */
    public SynchronizeJob create(Terminable t);
  }
}


/**********************************************************************
 * $Log: SynchronizeEngine.java,v $
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