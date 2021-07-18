/**********************************************************************
 *
 * Copyright (c) 2021 Olaf Willuhn
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
import de.willuhn.util.ApplicationException;

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

    try
    {
      HBCIProperties.checkIBAN(data.toString());
      qm.setData(Boolean.TRUE);
    }
    catch (ApplicationException ae)
    {
      qm.setData(Boolean.FALSE);
    }
  }
}

