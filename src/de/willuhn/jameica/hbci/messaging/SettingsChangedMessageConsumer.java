/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/SettingsChangedMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/10/14 14:29:35 $
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
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Wird benachrichtigt, wenn sich Hibiscus-Einstellungen geaendert haben.
 */
public class SettingsChangedMessageConsumer implements MessageConsumer
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
    return new Class[]{SettingsChangedMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    try
    {
      int ll = ((Integer) HBCI.LOGMAPPING.get(Logger.getLevel())).intValue();
      Logger.info("changing hbci4java loglevel to " + ll);
      HBCIUtils.setParam("log.loglevel.default",""+ ll);

      // Wir aktivieren/deaktivieren das SSL-Logging abhaengig vom Log-Level
      boolean b = Logger.getLevel().getValue() == Level.DEBUG.getValue();
      HBCIUtils.setParam("log.ssl.enable",b ? "1" : "0");
    }
    catch (Exception e)
    {
      Logger.write(Level.INFO,"unable to update hbci4java log level",e);
    }
  }

}


/*********************************************************************
 * $Log: SettingsChangedMessageConsumer.java,v $
 * Revision 1.2  2009/10/14 14:29:35  willuhn
 * @N Neuer HBCI4Java-Snapshot (2.5.11) - das SSL-Logging kann nun auch via HBCICallback in das jameica.log geleitet werden (wenn kein log.ssl.filename angegeben ist). Damit kann das Flag "log.ssl.enable" automatisch von Hibiscus aktiviert/deaktiviert werden, wenn das Jameica-Loglevel auf DEBUG oder !DEBUG steht
 *
 * Revision 1.1  2008/09/26 15:37:47  willuhn
 * @N Da das Messaging-System inzwischen Consumer solange sammeln kann, bis sie initialisiert ist, besteht kein Bedarf mehr, das explizite Registrieren von Consumern bis zum Versand der SystemMessage.SYSTEM_STARTED zu verzoegern
 *
 **********************************************************************/