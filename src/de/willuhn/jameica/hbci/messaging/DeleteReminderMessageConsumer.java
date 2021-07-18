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

import de.willuhn.jameica.hbci.reminder.ReminderUtil;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;

/**
 * Loescht den Reminder eines Auftrages, wenn der Auftrag selbst geloescht wird.
 */
public class DeleteReminderMessageConsumer implements MessageConsumer
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
    Object data      = msg.getData();
    if (!(data instanceof Terminable) || !(data instanceof HibiscusDBObject))
      return;

    // Dabei wird der Reminder geloescht
    ReminderUtil.apply((HibiscusDBObject) data,null,null);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false; // registriert in plugin.xml
  }

}

/**********************************************************************
 * $Log: DeleteReminderMessageConsumer.java,v $
 * Revision 1.1  2011/12/31 13:55:38  willuhn
 * @N Beim Loeschen eines Reminder-faehigen Auftrages wird der Reminder jetzt via Messaging automatisch gleich mit geloescht
 *
 **********************************************************************/