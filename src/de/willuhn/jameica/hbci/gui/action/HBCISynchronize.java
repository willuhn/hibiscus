/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/HBCISynchronize.java,v $
 * $Revision: 1.16 $
 * $Date: 2007/12/03 13:17:54 $
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
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SynchronizeJob;
import de.willuhn.jameica.hbci.server.hbci.AbstractHBCIJob;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.messaging.StatusBarMessage;
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

  private GenericIterator konten       = null;
  private GenericIterator selectedJobs = null;
  private boolean success              = false;
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    if (HBCIFactory.getInstance().inProgress())
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Derzeit läuft bereits eine HBCI-Synchronisierung"), StatusBarMessage.TYPE_ERROR));
      return;
    }

    Logger.info("Start synchronize");
    
    /////////////////////////////////////////////////////////////////
    // ggf. uebergebene Liste von manuell ausgewaehlten Jobs
    if (context != null && (context instanceof GenericIterator))
      this.selectedJobs = (GenericIterator) context;

    if (context != null && (context instanceof List))
    {
      try
      {
        List list = (List) context;
        this.selectedJobs = PseudoIterator.fromArray((GenericObject[])list.toArray(new GenericObject[list.size()]));
      }
      catch (RemoteException re)
      {
        Logger.error("unable to determine job list",re);
        throw new ApplicationException(i18n.tr("Fehler beim Ermitteln der HBCI-Aufträge"));
      }
    }
    /////////////////////////////////////////////////////////////////

    try
    {
      this.konten = SynchronizeEngine.getInstance().getSynchronizeKonten();
      success = true;
      sync(ProgressMonitor.STATUS_NONE);
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
  private void sync(int lastStatus)
  {

    // Globalen Status merken
    success &= (lastStatus == ProgressMonitor.STATUS_DONE || lastStatus == ProgressMonitor.STATUS_NONE);

    // Bei Abbruch brechen wir immer ab ;)
    if (lastStatus == ProgressMonitor.STATUS_CANCEL)
    {
      Logger.info("synchronize cancelled");
      GUI.getStatusBar().setErrorText(i18n.tr("Synchronisierung abgebrochen"));
      return;
    }
    
    // Wenn wir einen Fehler haben und wir in dem
    // Fall abbrechen sollen, dann tun wir das
    if (!success && Settings.getCancelSyncOnError())
    {
      Logger.warn("synchronize finished with errors. status: " + lastStatus);
      GUI.getStatusBar().setErrorText(i18n.tr("Synchronisierung mit Fehlern beendet"));
      return;
    }

    try
    {
      if (this.konten == null || !this.konten.hasNext())
      {
        this.konten       = null;
        this.selectedJobs = null;
        if (success)
        {
          Logger.info("synchronize finished");
          GUI.getStatusBar().setSuccessText(i18n.tr("Synchronisierung beendet"));
        }
        else
        {
          Logger.warn("synchronize finished with errors");
          GUI.getStatusBar().setErrorText(i18n.tr("Synchronisierung mit Fehlern beendet"));
        }

        // Seite neu laden
        // BUGZILLA 110 http://www.willuhn.de/bugzilla/show_bug.cgi?id=110
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
        return;
      }
      
      final Konto k = (Konto) this.konten.next();

      GUI.getStatusBar().setSuccessText(i18n.tr("Synchronisiere Konto {0}", k.getLongName()));
      Logger.info("synchronizing account: " + k.getLongName());

      // BUGZILLA 509
      ArrayList jobs = new ArrayList();
      GenericIterator list = SynchronizeEngine.getInstance().getSynchronizeJobs(k);
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
            jobs.add(currentJobs[i]);
        }
      }

      if (jobs.size() == 0)
      {
        Logger.info("nothing to do for account " + k.getLongName() + " - skipping");
        sync(ProgressMonitor.STATUS_NONE);
      }
      else
      {
        Logger.info("creating hbci factory");
        HBCIFactory factory = HBCIFactory.getInstance();
        for (int i=0;i<jobs.size();++i)
          factory.addJob((AbstractHBCIJob)jobs.get(i));

        factory.executeJobs(k,new Listener() {
          public void handleEvent(Event event)
          {
            try
            {
              sync(event.type);
            }
            catch (Exception e)
            {
              
            }
          }
        });
      }
    }
    catch (Exception e)
    {
      if (e instanceof ApplicationException)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      else
      {
        Logger.error("unable to sync konto",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen der HBCI-Synchronisierung"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }
}


/*********************************************************************
 * $Log: HBCISynchronize.java,v $
 * Revision 1.16  2007/12/03 13:17:54  willuhn
 * @N Debugging-Infos
 *
 * Revision 1.15  2007/11/30 18:37:08  willuhn
 * @B Bug 509
 *
 * Revision 1.14  2007/05/16 14:49:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2007/05/16 14:44:47  willuhn
 * @C Parallele Ausfuehrung mehrerer Synchronisierungen unterbinden
 *
 * Revision 1.12  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.11  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 * Revision 1.10  2007/05/14 12:50:41  willuhn
 * @B wrong list format
 *
 * Revision 1.9  2007/02/21 10:02:27  willuhn
 * @C Code zum Ausfuehren exklusiver Jobs redesigned
 *
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