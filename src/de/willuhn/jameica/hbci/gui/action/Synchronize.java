/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Synchronize.java,v $
 * $Revision: 1.18 $
 * $Date: 2007/12/05 22:45:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.synchronize.Synchronization;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
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
 * Hilfsklasse zum Ausfuehren der Synchronisierung.
 */
public class Synchronize implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private MessageConsumer mc = new MyMessageConsumer();
  private Iterator<Synchronization> list = null;

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   * Erwartet eine Liste mit Objekten des Typs {@link Synchronization}.
   */
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
    for (Object o:list)
    {
      if (!(o instanceof Synchronization))
      {
        Logger.warn("type " + o.getClass() + " is no valid synchronization");
        continue;
      }
      
      result.add((Synchronization) o);
    }
    
    Logger.info("synchronizing " + result.size() + " backends");
    this.list = result.iterator();
    
    // Auf die Events registrieren, um die Folge-Backends zu starten
    Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).registerMessageConsumer(this.mc);
    this.sync();
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
      Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Synchronisierung beendet"),StatusBarMessage.TYPE_SUCCESS));
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
    }
    catch (OperationCanceledException oce)
    {
      Application.getMessagingFactory().getMessagingQueue(SynchronizeBackend.QUEUE_STATUS).unRegisterMessageConsumer(this.mc);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Synchronisierung abgebrochen"),StatusBarMessage.TYPE_ERROR));
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
      
      Integer status = (Integer) data;
      if (status.intValue() == ProgressMonitor.STATUS_DONE)
      {
        sync();
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
