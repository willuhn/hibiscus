/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/Attic/StartupMessageConsumer.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/07/28 09:31:10 $
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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Fuehrt Kommandos aus, die erst dann erfolgen sollen, wenn
 * Jameica vollstaendig gestartet ist.
 */
public class StartupMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof SystemMessage))
      return;
    
    SystemMessage msg = (SystemMessage) message;
    if (msg.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;

    Application.getMessagingFactory().registerMessageConsumer(new MessageConsumer() {
      public void handleMessage(Message message) throws Exception
      {
        try
        {
          int ll = ((Integer) HBCI.LOGMAPPING.get(Logger.getLevel())).intValue();
          Logger.info("changing hbci4java loglevel to " + ll);
          HBCIUtils.setParam("log.loglevel.default",""+ ll);
        }
        catch (Exception e)
        {
          Logger.write(Level.INFO,"unable to update hbci4java log level",e);
        }
      }
      public Class[] getExpectedMessageTypes()
      {
        return new Class[]{SettingsChangedMessage.class};
      }
    
      public boolean autoRegister()
      {
        return false;
      }
    
    });

    Logger.info("register message consumers for query lookups");
    Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").registerMessageConsumer(new QueryHBCIVersionMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.bankname").registerMessageConsumer(new QueryBanknameMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.query.accountcrc").registerMessageConsumer(new QueryAccountCRCMessageConsumer());
    Application.getMessagingFactory().getMessagingQueue("hibiscus.transfer.lastschrift").registerMessageConsumer(new TransferLastschriftMessageConsumer());
  }

}


/*********************************************************************
 * $Log: StartupMessageConsumer.java,v $
 * Revision 1.4  2008/07/28 09:31:10  willuhn
 * @N Abfrage der HBCI-Version via Messaging
 *
 * Revision 1.3  2008/02/05 00:48:43  willuhn
 * @N Generischer MessageConsumer zur Erstellung von Lastschriften (Siehe Mail an Markus vom 04.02.2008)
 *
 * Revision 1.2  2008/01/25 12:24:05  willuhn
 * @B Messaging-Consumer zu frueh registriert
 *
 * Revision 1.1  2007/11/27 16:41:48  willuhn
 * @C MessageConsumers fuer Query-Lookups wurden zu frueh registriert
 *
 **********************************************************************/