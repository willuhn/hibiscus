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

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;


/**
 * Message-Consumer, der eine Konto/BLZ-Kombination auf Gueltigkeit pruefen kann.
 */
public class QueryAccountCRCMessageConsumer implements MessageConsumer
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
    String blz = data.toString();
    String[] s = blz.split(":");
    if (s == null || s.length != 2)
    {
      qm.setData(Boolean.FALSE);
      return;
    }
    
    qm.setData(Boolean.valueOf(HBCIProperties.checkAccountCRC(s[0],s[1])));
  }

}


/**********************************************************************
 * $Log: QueryAccountCRCMessageConsumer.java,v $
 * Revision 1.1  2007/11/12 00:08:02  willuhn
 * @N Query-Messages fuer Bankname-Lookup und CRC-Account-Check fuer JVerein
 *
 **********************************************************************/
