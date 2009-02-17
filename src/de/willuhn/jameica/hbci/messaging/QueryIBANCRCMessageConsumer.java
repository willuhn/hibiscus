/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/QueryIBANCRCMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/17 00:00:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;


/**
 * Message-Consumer, der eine IBAN auf Gueltigkeit pruefen kann.
 */
public class QueryIBANCRCMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // bewusst false, weil wir uns manuell auf eine konkret benannte Queue abonnieren.
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
    if (message == null || !(message instanceof QueryMessage))
      return;
    
    QueryMessage qm = (QueryMessage) message;
    Object data = qm.getData();
    if (data == null)
    {
      qm.setData(Boolean.FALSE);
      return;
    }
    qm.setData(new Boolean(HBCIProperties.checkIBANCRC(data.toString())));
  }

}


/**********************************************************************
 * $Log: QueryIBANCRCMessageConsumer.java,v $
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/