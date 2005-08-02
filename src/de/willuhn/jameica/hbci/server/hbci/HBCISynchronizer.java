/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/Attic/HBCISynchronizer.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/08/02 20:55:35 $
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
import java.util.ArrayList;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class HBCISynchronizer
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private I18N i18n = null;
  
  private Job[] jobs = null;
  private int index  = 0;
  
  /**
   * Startet die Synchronisierung.
   * @throws RemoteException 
   */
  public void start() throws RemoteException
  {
    Logger.info("Start synchronize");
    
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    DBIterator konten = Settings.getDBService().createList(Konto.class);

    if (konten == null || konten.size() == 0)
    {
      Logger.info("no accounts to syncronize");
      return;
    }

    Logger.info("creating synchronize list");
    ArrayList list = new ArrayList();
    
    while (konten.hasNext())
    {
      Konto k = (Konto) konten.next();
      if (!k.getSynchronize())
      {
        Logger.info("skipping konto " + k.getKontonummer());
        continue;
      }
      Logger.info("adding konto " + k.getKontonummer());
      list.add(new Job(k));
    }
    this.jobs = (Job[]) list.toArray(new Job[list.size()]);
    sync();
  }
  
  /**
   * Fuehrt die Syncronisierung des aktuellen Kontos durch.
   */
  private void sync()
  {
    try
    {
      if (index >= jobs.length)
      {
        Logger.info("syncing finished");
        GUI.getStatusBar().setStatusText(i18n.tr("Synchronisierung beendet"));
        GUI.getDisplay().asyncExec(new Runnable() {
          public void run()
          {
            GUI.getDisplay().timerExec(5000, new Runnable() {
              public void run()
              {
                GUI.getStatusBar().setStatusText("");
              }
            });
          }
        });
        return;
      }
      
      Job job = jobs[index++];
      Konto k = job.konto;

      GUI.getStatusBar().setStatusText(i18n.tr("Synchronisiere Konto {0} von {1}", new String[]{""+index,""+jobs.length}));
      
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
    catch (Exception e)
    {
      Logger.error("unable to sync konto",e);
    }
  }
  
  /**
   * Ein kleiner Container fuer die Jobs eines Kontos.
   */
  private class Job
  {
    private Konto konto                 = null;
    private Ueberweisung[] ueb          = null;
    private Dauerauftrag[] dauer        = null;
    private SammelLastschrift[] sammel  = null;
    private Lastschrift[] last          = null;
    
    private Job(Konto k) throws RemoteException
    {
      this.konto = k;
    }
  }
}


/*********************************************************************
 * $Log: HBCISynchronizer.java,v $
 * Revision 1.4  2005/08/02 20:55:35  web0
 * *** empty log message ***
 *
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