/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/HBCISynchronize.java,v $
 * $Revision: 1.8 $
 * $Date: 2006/10/09 21:43:26 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.server.SynchronizeEngine;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
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

  private I18N i18n = null;

  private GenericIterator selectedJobs = null;
  private Job[] jobs = null;
  private int index  = 0;
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Logger.info("Start synchronize");
    
    if (context != null && (context instanceof GenericIterator))
      this.selectedJobs = (GenericIterator) context;
    
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // Der Code hier sieht etwas umstaendlich aus. Das
    // machen wir, weil wir die HBCI-Jobs nach Konten gruppieren
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

      GenericIterator list = SynchronizeEngine.getInstance().getSynchronizeJobs(k);
      int count = 0;
      while (list.hasNext())
      {
        SynchronizeJob sj = (SynchronizeJob) list.next();
        
        if (this.selectedJobs != null && this.selectedJobs.contains(sj) == null)
        {
          Logger.info("skipping job " + sj.getName() + " - not selected");
          continue;
        }
        AbstractHBCIJob[] currentJobs = sj.createHBCIJobs();
        if (currentJobs != null)
        {
          for (int i=0;i<currentJobs.length;++i)
          {
            factory.addExclusiveJob(currentJobs[i]);
          }
        }
        count++;
      }

      if (count == 0)
      {
        Logger.info("nothing to do for account " + k.getLongName() + " - skipping");
        sync();
      }
      else
      {
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
 * Revision 1.8  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.7  2006/07/13 00:21:15  willuhn
 * @N Neue Auswertung "Sparquote"
 *
 * Revision 1.6  2006/07/05 22:18:16  willuhn
 * @N Einzelne Sync-Jobs koennen nun selektiv auch einmalig direkt in der Sync-Liste deaktiviert werden
 *
 * Revision 1.5  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
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