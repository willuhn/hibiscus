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

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.views.Start;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Wird ueber Status-Updates der Sync-Engine benachrichtigt.
 */
public class SyncEngineStatusMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
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
    
    final int status = ((Integer) data).intValue();
    
    // finaler Status.
    if (status == ProgressMonitor.STATUS_CANCEL ||
        status == ProgressMonitor.STATUS_DONE || 
        status == ProgressMonitor.STATUS_ERROR)
    {
      GUI.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          // Startseite neu laden, falls sie derzeit angezeigt wird.
          if (GUI.getCurrentView().getClass().equals(Start.class))
          {
            Logger.info("Reloading start view");
            GUI.startView(Start.class,null);
          }
          
          int statusbarType = StatusBarMessage.TYPE_SUCCESS;
          if (status == ProgressMonitor.STATUS_ERROR)
            statusbarType = StatusBarMessage.TYPE_ERROR;
          else if (status == ProgressMonitor.STATUS_CANCEL)
            statusbarType = StatusBarMessage.TYPE_INFO;
          
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Synchronisierung beendet"),statusbarType));
          
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


