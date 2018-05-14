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

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;


/**
 * Message-Consumer, der zu einer BLZ:Kontonummer die IBAN errechnen kann.
 */
public class QueryIBANCalcMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Registrierung per manifest auf benamte Queue.
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
      qm.setData(null);
      return;
    }
    String blz = data.toString();
    String[] s = blz.split(":");
    if (s == null || s.length != 2)
    {
      qm.setData(null);
      return;
    }
    try
    {
      IBAN iban=HBCIProperties.getIBAN(s[0],s[1]);
      String[] result=new String[]{iban.getIBAN(), iban.getBIC()};
      qm.setData(result);
    }
    catch(Exception e)
    {
      qm.setData(e);
    }
    
  }

}
