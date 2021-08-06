/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
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
  private MessageConsumer mcSync  = new SyncMessageConsumer();
  private MessageConsumer mcCache = new CacheMessageConsumer();
  private List<Synchronization> syncList = new ArrayList<Synchronization>();
  private Button syncButton = null;
  private Listener syncButtonListener = new SyncButtonListener();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeList() throws RemoteException
  {
    super(new Configure());
    this.addColumn(i18n.tr("Offene Synchronisierungsaufgaben"),"name");
    this.setSummary(false);
    this.setCheckable(true);
    this.setMulti(true);

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
    
    final ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Aktivieren"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        setChecked(true);
      }
    },"list-add.png"));
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Deaktivieren"),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        setChecked(false);
      }
    },"list-remove.png"));
    this.setContextMenu(menu);
    
    // Vorm paint() nochmal machen, damit auch XP die Spaltenbreiten hinkriegt - die werden wohl scheinbar beim paint() ermittelt
    init();
  }
  
  private void setChecked(boolean b)
  {
    Object o = getSelection();
    if (o == null)
      return;

    if (o instanceof Object[])
      setChecked((Object[])o,b);
    else
      setChecked(o,b);
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
        
        // Die Synchronisation brauchen wir nur dann zur Liste tun, wenn Jobs vorhanden sind
        if (jobs.size() > 0)
          this.syncList.add(sync);
      }
    }

    // Sync-Button-Status aktualisieren
    syncButtonListener.handleEvent(null);
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    if (this.syncButton != null)
      return;
    
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).registerMessageConsumer(this.mcSync);
    Application.getMessagingFactory().getMessagingQueue("jameica.gui.view.unbind").registerMessageConsumer(this.mcCache);
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(mcSync);
        Application.getMessagingFactory().getMessagingQueue("jameica.gui.view.unbind").unRegisterMessageConsumer(mcCache);
      }
    });

    this.syncButton = new Button(i18n.tr("S&ynchronisierung starten"),new SyncStart(),null,true,"mail-send-receive.png");
    
    // Aktualisieren den Button-Status je nach Auswahl
    this.addSelectionListener(this.syncButtonListener);
    
    super.paint(parent);
    
    // Erst nach dem paint() machen, damit der initiale Checked-State aus dem Cache beachtet wird
    this.init();
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("Synchronisierungsoptionen..."),new Options(),null,false,"document-properties.png"); // BUGZILLA 226
    b.addButton(this.syncButton);
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
          KontoAuswahlDialog d1 = new KontoAuswahlDialog(null,KontoFilter.SYNCED,KontoAuswahlDialog.POSITION_CENTER)
          {
            /**
             * @see de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog#getApplyButton()
             */
            @Override
            public Button getApplyButton()
            {
              Button b = super.getApplyButton();
              b.setText(i18n.tr("Synchronisationsoptionen anzeigen..."));
              return b;
            }
          };
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
        Logger.error("unable to configure synchronize options",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
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
   * Hilfsklasse zum Abfragen der Status-Codes der SynchronizeEngine.
   */
  private class SyncMessageConsumer implements MessageConsumer
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

            int i = status.intValue();
            
            if (i == ProgressMonitor.STATUS_RUNNING)
            {
              syncButton.setEnabled(false); // Sync-Button deaktivieren
              return;
            }

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
  
  /**
   * Wird beim unbind der View benachrichtigt und speichert die aktuelle Auswahl
   * der abgewaehlten Sync-Aufgaben.
   */
  private class CacheMessageConsumer implements MessageConsumer
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
    public void handleMessage(Message message) throws Exception
    {
      cacheUnchecked();
    }
  }
  
  /**
   * Aktualisiert den Status des Sync-Buttons.
   */
  private class SyncButtonListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      if (syncButton == null)
        return;
      
      try
      {
        List<SynchronizeJob> selected = getItems(true);
        syncButton.setEnabled(selected != null && selected.size() > 0);
      }
      catch (Exception e)
      {
        Logger.error("unable to determine selected items",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermitteln der Synchronisierungsaufgaben"),StatusBarMessage.TYPE_ERROR));
      }
    }
  }

}
