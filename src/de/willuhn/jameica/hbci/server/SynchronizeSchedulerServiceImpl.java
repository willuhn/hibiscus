/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeSchedulerSettings;
import de.willuhn.jameica.hbci.gui.action.Synchronize;
import de.willuhn.jameica.hbci.gui.parts.SynchronizeList;
import de.willuhn.jameica.hbci.rmi.SynchronizeSchedulerService;
import de.willuhn.jameica.hbci.synchronize.Synchronization;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.messaging.Message;
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
 * Implementierung des Scheduler-Services für die automatische Synchronisierung.
 */
public class SynchronizeSchedulerServiceImpl extends UnicastRemoteObject implements SynchronizeSchedulerService
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Timer timer        = null;
  private SchedulerTask task = null;
  private long period        = 1;
  private int status         = ProgressMonitor.STATUS_NONE;
  private long lastStart     = 0;
  private long lastFinish    = 0;
  private MessageConsumer mc = new MyMessageConsumer();

  /**
   * ct.
   * @throws RemoteException
   */
  public SynchronizeSchedulerServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Scheduler-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return SynchronizeSchedulerSettings.isEnabled() && !isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.timer != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (!SynchronizeSchedulerSettings.isEnabled())
      return;
    
    if (isStarted())
    {
      Logger.warn("service already started, skipping request");
      return;
    }
    
    Logger.info("starting scheduler service");
    int interval = SynchronizeSchedulerSettings.getSchedulerInterval();
    Logger.info("scheduler interval: " + interval + " minutes");

    this.timer  = new Timer();
    this.task   = new SchedulerTask();
    this.period = interval * 60 * 1000l;
    
    timer.schedule(this.task,60 * 1000L,period);
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).registerMessageConsumer(this.mc);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeSchedulerService#getStatus()
   */
  @Override
  public int getStatus() throws RemoteException
  {
    return this.status;
  }
  
  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }

    if (this.timer == null)
    {
      Logger.info("skip stop request. Scheduler not running");
      return;
    }

    try
    {
      Logger.info("stopping scheduler service");
      Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
      this.task.cancel();
      this.timer.cancel();
    }
    finally
    {
      this.task  = null;
      this.timer = null;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SynchronizeSchedulerService#getNextExecution()
   */
  public Date getNextExecution() throws RemoteException
  {
    if (this.task == null)
      return null;

    long current = this.task.scheduledExecutionTime();
    Logger.info("calculating next execution [scheduled execution: " + new Date(current) + ", last start: " + (this.lastStart <= 0 ? "<never>" : new Date(this.lastStart)) + ", last finish: " + (this.lastFinish <= 0 ? "<never>" : new Date(this.lastFinish)) + "]");
    Date d = null;
    // Wir suchen nach der naechsten Ausfuehrungszeit
    for (int i=0;i<10000;++i)
    {
      current += this.period;
      d = new Date(current);
      if (this.canRun(d) && current > System.currentTimeMillis())
      {
        Logger.info("next execution: " + d);
        return d;
      }
    }
    Logger.error("exclude window too large, scheduler will ne run");
    Application.getMessagingFactory().sendMessage(new StatusBarMessage("Zeitfenster für Ausschluss zu groß, Synchronisierung würde nie starten", StatusBarMessage.TYPE_ERROR));
    return null;
  }
  
  /**
   * Führt die eigentliche Synchronisierung aus.
   */
  private void doSync()
  {
    try
    {
      final List<Synchronization> list = SynchronizeList.getActiveSyncs();
      if (list.isEmpty())
        return;
      
      Synchronize sync = new Synchronize();
      sync.handleAction(list);
    }
    catch (OperationCanceledException oce)
    {
      // ignore
      Logger.info("synchronization cancelled");
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while synchronizing",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Synchronisierung fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Prueft, ob der Aufruf innerhalb der Ausschluss-Zeit stattfindet.
   * @param check die zu pruefende Zeit.
   * @return true, wenn der Aufruf NICHT in der Ausschluss-Zeit stattfindet und laufen darf.
   */
  private boolean canRun(Date check)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(check);

    // Ausschluss-Tag?
    if (!SynchronizeSchedulerSettings.getSchedulerIncludeDay(cal.get(Calendar.DAY_OF_WEEK)))
      return false;

    // Uhrzeit checken
    int from = SynchronizeSchedulerSettings.getSchedulerStartTime();
    int to   = SynchronizeSchedulerSettings.getSchedulerEndTime();

    // Von/Bis ist identisch. Dann setzt er nie aus
    if (from == to)
      return true;

    // Checken, ob wir uns innerhalb des Zeit-Fensters befinden
    int run = cal.get(Calendar.HOUR_OF_DAY);
    if (from > to) // Tageswechsel dazwischen?
      return run >= from || run < to;

    return run >= from && run < to;
  }

  /**
   * Implementierung des Timer-Tasks.
   */
  public class SchedulerTask extends TimerTask
  {
    /**
     * ct.
     */
    public SchedulerTask()
    {
      Logger.info("starting scheduled synchronization");
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      try
      {
        if (!canRun(new Date()))
        {
          Logger.info("skip synchronize, not inside execution time [hours: " + SynchronizeSchedulerSettings.getSchedulerStartTime() + " - " + SynchronizeSchedulerSettings.getSchedulerEndTime() + "]");
          return;
        }

        lastStart = System.currentTimeMillis();
        doSync();
      }
      catch (Exception e)
      {
        Logger.error("error while executing scheduler task: " + e.getMessage(),e);
      }
      finally
      {
        lastFinish = System.currentTimeMillis();
      }
    }
  }

  /**
   * Wird ueber die Status-Events der Backends benachrichtigt und startet dann das naechste
   */
  private class MyMessageConsumer implements MessageConsumer
  {
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
      QueryMessage msg = (QueryMessage) message;
      Object data = msg.getData();
      if (!(data instanceof Integer))
      {
        Logger.warn("got unknown data: " + data);
        return;
      }
      
      status = ((Integer) data).intValue();
      
      if (status == ProgressMonitor.STATUS_ERROR && SynchronizeSchedulerSettings.getStopSchedulerOnError())
      {
        Logger.error("stopping synchronize scheduler");
        try
        {
          stop(true);
        }
        catch (RemoteException re)
        {
          Logger.error("stopping of service failed");
        }
      }
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

}
