/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.forecast.ForecastCreator;
import de.willuhn.jameica.hbci.forecast.SaldoLimit;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;

/**
 * Aktualisiert automatisch die Saldo-Limits bei Bedarf.
 */
public class SaldoLimits implements MessageConsumer
{
  private static Timer timer        = null;
  private static SchedulerTask task = null;
  private static DelayedListener listener = new DelayedListener(1000,new Worker());

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SaldoMessage.class,SaldoLimitsMessage.class,SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (message instanceof SystemMessage)
    {
      final SystemMessage msg = (SystemMessage) message;
      if (msg.getStatusCode() == SystemMessage.SYSTEM_STARTED)
      {
        update();
        timer  = new Timer();
        task   = new SchedulerTask();
        timer.schedule(task,30 * 60 * 1000L,30 * 60 * 1000L);
      }
    }
    
    if ((message instanceof SaldoMessage) || message instanceof SaldoLimitsMessage)
    {
      listener.handleEvent(null);
    }
  }

  /**
   * Fuehrt das Update durch.
   */
  private static void update()
  {
    ForecastCreator.updateLimits();
    
    for (SaldoLimit limit:ForecastCreator.getLimits())
    {
      if(!limit.isNotify())
        continue;
      
      // TODO Benachrichtigung anzeigen
    }
  }
  
  /**
   * Implementierung des Timer-Tasks.
   */
  private static class SchedulerTask extends TimerTask
  {
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      update();
    }
  }
  
  private static class Worker implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      update();
    }
  }
}
