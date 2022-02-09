/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeExecuteDialog;
import de.willuhn.jameica.hbci.synchronize.Synchronization;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageBus;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class Synchronize implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private MessageConsumer mc = new MyMessageConsumer();
  private Iterator<Synchronization> list = null;

  /**
   * Erwartet eine Liste mit Objekten des Typs {@link Synchronization}.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Logger.info("Start synchronization");

    if (!(context instanceof List))
      throw new ApplicationException(i18n.tr("Keine Synchronisierungsaufgaben ausgewählt"));

    List list = (List) context;
    if (list.size() == 0)
      throw new ApplicationException(i18n.tr("Keine Synchronisierungsaufgaben ausgewählt"));

    Logger.info("backends to synchronize: " + list.size());
    List<Synchronization> result = new ArrayList<Synchronization>();
    List<SynchronizeJob> nonRecurring = new ArrayList<SynchronizeJob>();
    for (Object o:list)
    {
      if (!(o instanceof Synchronization))
      {
        Logger.warn("type " + o.getClass() + " is no valid synchronization");
        continue;
      }

      Synchronization sync = (Synchronization) o;
      List<SynchronizeJob> jobs = sync.getJobs();
      for (SynchronizeJob job:jobs)
      {
        if (!job.isRecurring())
          nonRecurring.add(job);
      }
      result.add(sync);
    }
    
    this.checkNonRecurring(nonRecurring);
    
    Logger.info("synchronizing " + result.size() + " backends");
    this.list = result.iterator();
    
    MessageBus.send(SynchronizeEngine.STATUS,ProgressMonitor.STATUS_RUNNING);
    // Auf die Events registrieren, um die Folge-Backends zu starten
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).registerMessageConsumer(this.mc);
    this.sync();
  }
  
  /**
   * Zeigt nochmal einen Warndialog an, wenn in der Synchronisation Auftraege
   * enthalten, die Geld bewegen. Dann hat der User die Chance, den Vorgang
   * noch abzubrechen, falls er sie auf der Startseite in der Liste der Synchronisierungsaufgaben
   * uebersehen hat - z.Bsp. weil er viele Konten hat und die Ueberweisungen ausserhalb
   * des sichtbaren Bereichs waren.
   * @param jobs die Liste der Auftraege.
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  private void checkNonRecurring(List<SynchronizeJob> jobs) throws ApplicationException, OperationCanceledException
  {
    if (jobs == null || jobs.size() == 0)
      return;
    
    if (Application.inServerMode())
      return;

    try
    {
      SynchronizeExecuteDialog d = new SynchronizeExecuteDialog(jobs,SynchronizeExecuteDialog.POSITION_CENTER);
      d.open();
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("error while checking jobs",e);
      throw new ApplicationException(i18n.tr("Fehler beim Ausführen der Aufträge: {0}",e.getMessage()));
    }
  }
  
  /**
   * Startet den naechsten Durchlauf.
   * @throws ApplicationException
   */
  private void sync()
  {
    if (!this.list.hasNext())
    {
      Logger.info("no more backends. synchronization done");
      finish(ProgressMonitor.STATUS_DONE);
      return;
    }
    
    try
    {
      // Sonst naechste Iteration starten
      Synchronization s = this.list.next();
      SynchronizeBackend backend = s.getBackend();
      List<SynchronizeJob> jobs = s.getJobs();
      Logger.info("synchronizing backend " + backend.getName() + " with " + jobs.size() + " jobs");
      backend.execute(jobs);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      MessageBus.send(SynchronizeEngine.STATUS,ProgressMonitor.STATUS_ERROR);
    }
    catch (OperationCanceledException oce)
    {
      Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Synchronisierung abgebrochen"),StatusBarMessage.TYPE_ERROR));
      MessageBus.send(SynchronizeEngine.STATUS,ProgressMonitor.STATUS_CANCEL);
    }
  }
  
  /**
   * Beendet die Synchronisierung mit dem angegebenen Status.
   * @param status der Status.
   */
  private void finish(int status)
  {
    MessageBus.send(SynchronizeEngine.STATUS,status);
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
  }
  
  /**
   * Wird ueber die Status-Events der Backends benachrichtigt und startet dann das naechste
   */
  private class MyMessageConsumer implements MessageConsumer
  {
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{QueryMessage.class};
    }

    @Override
    public void handleMessage(Message message) throws Exception
    {
      QueryMessage msg = (QueryMessage) message;
      Object data = msg.getData();
      if (!(data instanceof Integer))
      {
        Logger.warn("got unknown data: " + data);
        return;
      }
      
      int status = ((Integer) data).intValue();
      if (status == ProgressMonitor.STATUS_DONE)
      {
        sync();
      }
      else if (status == ProgressMonitor.STATUS_ERROR || status == ProgressMonitor.STATUS_CANCEL)
      {
        finish(status);
      }
    }

    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
