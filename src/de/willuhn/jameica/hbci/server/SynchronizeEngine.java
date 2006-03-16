/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/SynchronizeEngine.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/16 18:23:36 $
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
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
   * Liefert eine Liste von verfuegbaren Synchronize-Jobs.
   * @return Liste der gefundenen Jobs.
   * @throws RemoteException
   */
  public GenericIterator getSynchronizeJobs() throws RemoteException
  {
    DBIterator konten = Settings.getDBService().createList(Konto.class);
    konten.addFilter("synchronize = 1");
    
    ArrayList jobs = new ArrayList();
    while (konten.hasNext())
    {
      findJobs((Konto) konten.next(),jobs);
    }
    return PseudoIterator.fromArray((SynchronizeJob[]) jobs.toArray(new SynchronizeJob[jobs.size()]));
  }

  /**
   * Sucht nach allen Synchronisierungsjobs fuer dieses Konto und hangt sie an die Liste.
   * @param k Konto.
   * @param list Liste, an die die Jobs gehaengt werden.
   */
  private void findJobs(Konto k, ArrayList list)
  {
    // TODO hier weiter 
  }
}


/**********************************************************************
 * $Log: SynchronizeEngine.java,v $
 * Revision 1.1  2006/03/16 18:23:36  willuhn
 * @N first code for new synchronize system
 *
 **********************************************************************/