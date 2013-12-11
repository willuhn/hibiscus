/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SynchronizeList.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/12/27 22:47:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Synchronize;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeOptionsDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.Synchronization;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Vorgefertigte Liste mit den offenen Synchronisierungsaufgaben.
 */
public class SynchronizeList extends TablePart
{
  // Wir cachen die Liste der vom User explizit abgewaehlten Aufgaben waehrend der Sitzung
  private static Map<String,Boolean> uncheckedCache = new HashMap<String,Boolean>();
  
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private MessageConsumer mc = new MyMessageConsumer();
  private List<Synchronization> syncList = new ArrayList<Synchronization>();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeList() throws RemoteException
  {
    super(new Configure());
    this.addColumn(i18n.tr("Offene Synchronisierungsaufgaben"),"name");
    this.setSummary(false);
    this.setRememberColWidths(true);
    this.setCheckable(true);

    // BUGZILLA 583
    this.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        try
        {
          if (item == null)
            return;
          SynchronizeJob job = (SynchronizeJob) item.getData();
          if (job == null)
            return;
          item.setFont(job.isRecurring() ? Font.DEFAULT.getSWTFont() : Font.BOLD.getSWTFont());
        }
        catch (Exception e)
        {
          Logger.error("unable to format text",e);
        }
      }
    });
  }
  
  /**
   * Initialisiert die Liste der Synchronisierungsaufgaben
   * @throws RemoteException
   */
  private void init() throws RemoteException
  {
    this.syncList.clear();
    
    this.removeAll(); // leer machen
    
    // Liste der Sync-Jobs hinzufuegen
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    List<SynchronizeBackend> backends = service.get(SynchronizeEngine.class).getBackends();
    for (SynchronizeBackend backend:backends)
    {
      Synchronization sync = new Synchronization();
      sync.setBackend(backend);
      List<SynchronizeJob> jobs = backend.getSynchronizeJobs(null); // fuer alle Konten
      if (jobs != null)
      {
        for (SynchronizeJob job:jobs)
        {
          boolean checked = true;
          try
          {
            // nicht markiert, wenn das letzte Mal explizit abgewaehlt
            checked = !uncheckedCache.containsKey(job.getName());
          }
          catch (Exception e)
          {
            Logger.error("unable to determine if job was unchecked",e);
          }
          this.addItem(job,checked);

          sync.getJobs().add(job);
        }
      }
      this.syncList.add(sync);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).registerMessageConsumer(this.mc);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(mc);
      }
    });

    final Button start = new Button(i18n.tr("S&ynchronisierung starten"),new SyncStart(),null,true,"mail-send-receive.png");
    
    // Button deaktivieren, wenn alle Sync-Aufgaben abgeschaltet sind
    this.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          SynchronizeJob job = (SynchronizeJob) event.data;
          if (job != null)
          {
            TableItem item = (TableItem) event.item;
            boolean b = item.getChecked();
            String key = job.getName();
            if (b)
              uncheckedCache.remove(key);
            else
              uncheckedCache.put(key,Boolean.TRUE);
          }
          List<SynchronizeJob> selected = getItems(true);
          start.setEnabled(selected != null && selected.size() > 0);
        }
        catch (Exception e)
        {
          Logger.error("unable to determine selected items",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermitteln der Synchronisierungsaufgaben"),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    
    super.paint(parent);
    
    // Erst nach dem paint() machen, damit der initiale Checked-State aus dem Cache beachtet wird
    this.init();
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("Optionen..."),new Options(),null,false,"document-properties.png"); // BUGZILLA 226
    b.addButton(start);
    b.paint(parent);
  }
  
  /**
   * Cached die Liste der abgewaehlten Aufgaben.
   */
  private void cacheUnchecked()
  {
    try
    {
      uncheckedCache.clear();
      List<SynchronizeJob> selected = getItems(true);
      List<SynchronizeJob> all      = getItems(false);
      for (SynchronizeJob j:all)
      {
        if (!selected.contains(j))
        {
          // abgewaehlt
          uncheckedCache.put(j.getName(),Boolean.TRUE);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to cache unchecked items",e);
    }
  }
  
  /**
   * Oeffnet den Dialog mit den Synchronisierungsoptionen.
   */
  private class Options implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        Konto k  = null;
        Object o = getSelection();
        if (o instanceof SynchronizeJob)
          k = ((SynchronizeJob)o).getKonto();
        
        // Konto erfragen
        if (k == null)
        {
          KontoAuswahlDialog d1 = new KontoAuswahlDialog(null,KontoFilter.SYNCED,KontoAuswahlDialog.POSITION_CENTER);
          d1.setText(i18n.tr("Bitte wählen Sie das Konto, für welches Sie die " +
                             "Synchronisierungsoptionen ändern möchten."));
          k = (Konto) d1.open();
        }
        
        if (k == null)
          return;

        SynchronizeOptionsDialog d = new SynchronizeOptionsDialog(k,SynchronizeOptionsDialog.POSITION_CENTER);
        d.open();
        
        // So, jetzt muessen wir die Liste der Sync-Jobs neu befuellen
        init();
      }
      catch (OperationCanceledException oce)
      {
        // ignore
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("unable to configure synchronize options");
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen: {0}",e.getMessage()));
      }
    }
  }
  
  /**
   * Startet die Synchronisierung der Konten.
   */
  private class SyncStart implements Action
  {
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        cacheUnchecked();
        
        Logger.info("Collecting synchronize jobs");
        List<SynchronizeJob> selected = getItems(true);

        // Iterieren ueber die Synchronisationen und die rauswerfen, die nicht markiert sind
        List<Synchronization> result = new ArrayList<Synchronization>();
        for (Synchronization s:syncList)
        {
          List<SynchronizeJob> jobs = s.getJobs(); // komplette Liste der Jobs
          List<SynchronizeJob> toExecute = new ArrayList<SynchronizeJob>();
          for (SynchronizeJob job:jobs)
          {
            if (selected.contains(job)) // in den selektierten enthalten?
              toExecute.add(job);
          }
          
          // Gar kein Job in dem Backend ausgewaehlt
          if (toExecute.size() == 0)
            continue;
          
          Synchronization rs = new Synchronization();
          rs.setBackend(s.getBackend());
          rs.setJobs(toExecute);
          result.add(rs);
        }
        
        Synchronize sync = new Synchronize();
        sync.handleAction(result);
      }
      catch (OperationCanceledException oce)
      {
        // ignore
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (RemoteException re)
      {
        Logger.error("error while synchronizing",re);
        throw new ApplicationException(i18n.tr("Synchronisierung fehlgeschlagen: {0}",re.getMessage()));
      }
    }
  }

  /**
   * Hilfsklasse zum Reagieren auf Doppelklicks in der Liste.
   * Dort stehen naemlich ganz verschiedene Datensaetze drin.
   * Daher muss der Datensatz selbst entscheiden, was beim
   * Klick auf ihn gesehen soll.
   */
  private static class Configure implements Action
  {

    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (!(context instanceof SynchronizeJob))
        return;
      
      ((SynchronizeJob)context).configure();
    }
  }
  
  /**
   * Hilfsklasse zum Abfragen der Status-Codes von der HBCI-Factory.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          try
          {
            QueryMessage msg = (QueryMessage) message;
            Integer status = (Integer) msg.getData();
            if (status == null)
              return;

            if (status.intValue() == ProgressMonitor.STATUS_RUNNING)
              return; // laeuft noch
            
            // Liste der Jobs aktualisieren
            init();
          }
          catch (Exception e)
          {
            Logger.error("unable to reload sync jobs",e);
          }
        }
      });
    }
  }

}
