/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/Attic/HBCISynchronizer.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/08/01 23:27:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class HBCISynchronizer
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private DBIterator konten = null;
  
  /**
   * Startet die Synchronisierung.
   * @throws RemoteException 
   */
  public void start() throws RemoteException
  {
    Logger.info("Start synchronize");
    this.konten = Settings.getDBService().createList(Konto.class);
    if (this.konten == null || this.konten.size() == 0)
    {
      Logger.info("no accounts to syncronize");
      return;
    }
    sync();
  }
  
  /**
   * Fuehrt die Syncronisierung des aktuellen Kontos durch.
   */
  private void sync()
  {
    try
    {
      if (konten == null || !konten.hasNext())
      {
        Logger.info("syncing finished");
        return;
      }
      
      Konto k = (Konto) konten.next();
      
      Logger.info("checking if konto " + k.getKontonummer() + " has to be synced");

      if (k.getSynchronize())
      {
        Logger.info("synchronizing konto: " + k.getKontonummer());

        Logger.info("creating hbci factory");
        HBCIFactory factory = HBCIFactory.getInstance();

        Logger.info("adding umsatz job");
        factory.addJob(new HBCIUmsatzJob(k));
        
        Logger.info("adding saldo job");
        factory.addExclusiveJob(new HBCISaldoJob(k));

        if (settings.getBoolean("sync.ueb",false))
        {
          Logger.info("adding open transfers");
          DBIterator list = k.getUeberweisungen();
          while (list.hasNext())
          {
            Ueberweisung u = (Ueberweisung) list.next();
            if (u.ausgefuehrt())
              continue;
            if (u.ueberfaellig())
            {
              factory.addExclusiveJob(new HBCIUeberweisungJob(u));
            }
          }
        }
        if (settings.getBoolean("sync.last",false))
        {
          Logger.info("adding open transfers");
          DBIterator list = k.getLastschriften();
          while (list.hasNext())
          {
            Lastschrift u = (Lastschrift) list.next();
            if (u.ausgefuehrt())
              continue;
            if (u.ueberfaellig())
            {
              factory.addExclusiveJob(new HBCILastschriftJob(u));
            }
          }
          list = k.getSammelLastschriften();
          while (list.hasNext())
          {
            SammelLastschrift u = (SammelLastschrift) list.next();
            if (u.ausgefuehrt())
              continue;
            if (u.ueberfaellig())
            {
              factory.addExclusiveJob(new HBCISammelLastschriftJob(u));
            }
          }
        }
        if (settings.getBoolean("sync.dauer",false))
        {
          factory.addExclusiveJob(new HBCIDauerauftragListJob(k));

          Logger.info("adding open transfers");
          DBIterator list = k.getDauerauftraege();
          while (list.hasNext())
          {
            Dauerauftrag u = (Dauerauftrag) list.next();
            if (u.isActive())
              continue;
            factory.addExclusiveJob(new HBCIDauerauftragStoreJob(u));
          }
        }
        factory.executeJobs(k,new Listener() {
          public void handleEvent(Event event)
          {
            // Nach Abschluss das naechste syncronisieren
            sync();
          }
        
        });
      }
      else
      {
        Logger.info("skipping konto " + k.getKontonummer());
        sync();
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to sync konto",e);
    }
  }
}


/*********************************************************************
 * $Log: HBCISynchronizer.java,v $
 * Revision 1.3  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/08/01 20:35:31  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 **********************************************************************/