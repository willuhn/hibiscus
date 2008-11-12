/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/InfoPointMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/11/12 15:48:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.logging.Logger;

/**
 * Wird benachrichtigt, wenn HBCI4Java die Bank-Parameter an den
 * Infopoint-Server senden will.
 */
public class InfoPointMessageConsumer implements MessageConsumer
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
    QueryMessage msg = (QueryMessage) message;
    // Wir loggen das erstmal nur, senden aber noch nichts.
    Logger.info("got infopoint ack request: " + msg.getName());
    Logger.debug("xml data: " + msg.getData());
    msg.setData(Boolean.FALSE);
  }

}


/*********************************************************************
 * $Log: InfoPointMessageConsumer.java,v $
 * Revision 1.2  2008/11/12 15:48:17  willuhn
 * @C changed loglevel
 *
 * Revision 1.1  2008/11/04 11:55:23  willuhn
 * @N Update auf HBCI4Java 2.5.9
 *
 **********************************************************************/