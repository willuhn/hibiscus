/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.views.Start;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

/**
 * Wird ueber Status-Updates der Sync-Engine benachrichtigt.
 */
public class SyncEngineStatusMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    Object data = msg.getData();
    if (!(data instanceof Integer))
      return;
    
    int status = ((Integer) data).intValue();
    
    // finaler Status.
    if (status == ProgressMonitor.STATUS_CANCEL ||
        status == ProgressMonitor.STATUS_DONE || 
        status == ProgressMonitor.STATUS_ERROR)
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run()
        {
          // Startseite neu laden, falls sie derzeit angezeigt wird.
          if (GUI.getCurrentView().getClass().equals(Start.class))
          {
            Logger.info("Reloading start view");
            GUI.startView(Start.class,null);
          }
        }
      });
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    // per plugin.xml registriert
    return false;
  }

}


