/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/HBCISynchronize.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/03/15 16:25:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragListJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIDauerauftragStoreJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.HBCILastschriftJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISaldoJob;
import de.willuhn.jameica.hbci.server.hbci.HBCISammelLastschriftJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUeberweisungJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIUmsatzJob;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class HBCISynchronize implements Action
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronize.class);
  
  private I18N i18n = null;
  
  private Job[] jobs = null;
  private int index  = 0;
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Logger.info("Start synchronize");
    
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    try
    {
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
      Logger.info("accounts to synchronize: " + this.jobs.length);
      sync();
    }
    catch (RemoteException e)
    {
      Logger.error("error while syncing",e);
      throw new ApplicationException(i18n.tr("Fehler beim Synchronisieren der Konten"),e);
    }
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
        Logger.info("synchronize finished");
        GUI.getStatusBar().setSuccessText(i18n.tr("Synchronisierung beendet"));

        // Seite neu laden
        // BUGZILLA 110 http://www.willuhn.de/bugzilla/show_bug.cgi?id=110
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
        return;
      }
      
      final Job job = jobs[index++];
      final Konto k = job.konto;

      GUI.getStatusBar().setSuccessText(i18n.tr("Synchronisiere Konto {0} von {1}", new String[]{""+index,""+jobs.length}));
      
      Logger.info("synchronizing account: " + k.getKontonummer());

      Logger.info("creating hbci factory");
      HBCIFactory factory = HBCIFactory.getInstance();

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

      // Umsaetze und Salden werden zum Schluss ausgefuehrt,
      // damit die oben gesendeten Ueberweisungen gleich mit
      // erscheinen, insofern die Bank das unterstuetzt.
      Logger.info("adding umsatz job");
      factory.addJob(new HBCIUmsatzJob(k));
      
      Logger.info("adding saldo job");
      factory.addExclusiveJob(new HBCISaldoJob(k));
      
      factory.executeJobs(k,new Listener() {
        public void handleEvent(Event event)
        {
          if (event.type == ProgressMonitor.STATUS_DONE)
          {
            // Nach erfolgreichem Abschluss das naechste syncronisieren
            sync();
          }
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
    private Konto konto = null;
    
    private Job(Konto k)
    {
      this.konto = k;
    }
  }
}


/*********************************************************************
 * $Log: HBCISynchronize.java,v $
 * Revision 1.4  2006/03/15 16:25:48  willuhn
 * @N Statusbar refactoring
 *
 * Revision 1.3  2006/02/20 22:28:57  willuhn
 * @B bug 178
 *
 * Revision 1.2  2006/02/06 17:16:11  willuhn
 * @B Fehler beim Synchronisieren mehrerer Konten (Dead-Lock)
 *
 * Revision 1.1  2006/01/11 00:29:22  willuhn
 * @C HBCISynchronizer nach gui.action verschoben
 * @R undo bug 179 (blendet zu zeitig aus, wenn mehrere Jobs (Synchronize) laufen)
 *
 * Revision 1.7  2005/11/07 22:42:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/08/08 17:04:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2005/08/05 16:33:42  willuhn
 * @B bug 108
 * @B bug 110
 *
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