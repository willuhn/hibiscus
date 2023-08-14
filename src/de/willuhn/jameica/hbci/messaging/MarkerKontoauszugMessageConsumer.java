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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Markiert das Navigations-Element "Auswertungen-&gt;Kontoauszuege", wenn neue Umsaetze vorhanden sind.
 */
public class MarkerKontoauszugMessageConsumer implements MessageConsumer
{
  private DelayedListener listener = new DelayedListener(1000,new Worker());
  
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class, ObjectDeletedMessage.class, ObjectChangedMessage.class, SystemMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (Application.inServerMode())
      return;

    if (message instanceof SystemMessage)
    {
      SystemMessage msg = (SystemMessage) message;
      if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
        return;
    }
    
    if (message instanceof ObjectMessage)
    {
      GenericObject o = ((ObjectMessage)message).getObject();
      if (!(o instanceof Kontoauszug))
        return;
    }

    listener.handleEvent(null);
  }

  @Override
  public boolean autoRegister()
  {
    return true;
  }
  
  /**
   * Der eigentliche Worker.
   */
  private class Worker implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        GenericIterator i = KontoauszugPdfUtil.getUnread();
        final int size = i.size();
        GUI.getNavigation().setUnreadCount("hibiscus.navi.kontoauszug",size);
      }
      catch (Throwable t)
      {
        Logger.write(Level.DEBUG,"unable to update navigation",t);
      }
    }
    
  }
}
