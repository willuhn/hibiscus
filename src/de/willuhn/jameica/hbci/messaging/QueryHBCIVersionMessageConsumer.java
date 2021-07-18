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

import de.willuhn.jameica.hbci.gui.dialogs.HBCIVersionDialog;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;

/**
 * Beantwortet Abfragen zur HBCI-Version.
 */
public class QueryHBCIVersionMessageConsumer implements MessageConsumer
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
    if (Application.inServerMode())
      return; // Wenn wir nicht mit GUI laufen, ignorieren wir die Nachricht
    QueryMessage msg = (QueryMessage) message;
    HBCIVersionDialog d = new HBCIVersionDialog(HBCIVersionDialog.POSITION_CENTER);
    msg.setData(d.open());
  }
}

/*********************************************************************
 * $Log: QueryHBCIVersionMessageConsumer.java,v $
 * Revision 1.1  2008/07/28 09:31:10  willuhn
 * @N Abfrage der HBCI-Version via Messaging
 *
 **********************************************************************/