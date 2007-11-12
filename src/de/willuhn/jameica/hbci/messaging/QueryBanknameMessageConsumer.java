/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/QueryBanknameMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/11/12 00:08:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;


/**
 * Message-Consumer, der zu einer BLZ den Namen der Bank liefern kann.
 */
public class QueryBanknameMessageConsumer implements MessageConsumer
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
      qm.setData(""); // Kein Bankname
      return;
    }
    String blz = data.toString();
    qm.setData(HBCIUtils.getNameForBLZ(blz));
  }

}


/**********************************************************************
 * $Log: QueryBanknameMessageConsumer.java,v $
 * Revision 1.1  2007/11/12 00:08:02  willuhn
 * @N Query-Messages fuer Bankname-Lookup und CRC-Account-Check fuer JVerein
 *
 **********************************************************************/
