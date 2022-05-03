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

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;

/**
 * Ermittelt, ob das Messaging-System zur Archivierung von Kontoauszuegen verfuegbar ist.
 * Da das Lookup per Lookup-Service bis zu 5 Sekunden dauern kann (Timeout, wenn kein
 * Server erreichbar ist), machen wir das per Messaging beim Boot-Vorgang im Hintergrund.
 * Dann muss der User anschliessend nicht warten, wenn die Information benoetigt wird.
 */
public class MessagingAvailableConsumer implements MessageConsumer
{
  private static Boolean HAVE_MESSAGING = null;
  
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    new Thread(new Runnable() {
      
      @Override
      public void run()
      {
        haveMessaging();
      }
    }).start();
  }
  
  /**
   * Prueft, ob das Speichern per Messaging grundsaetzlich moeglich ist.
   * @return true, wenn das Speichern per Messaging grundsaetzlich moeglich ist.
   */
  public static boolean haveMessaging()
  {
    if (HAVE_MESSAGING != null)
      return HAVE_MESSAGING;
    
    // Messaging-Plugin ist lokal installiert
    HAVE_MESSAGING = Application.getPluginLoader().getPlugin("de.willuhn.jameica.messaging.Plugin") != null;
    
    // Alternativ per TCP-Connector auf einer anderen Instanz
    if (!HAVE_MESSAGING)
      HAVE_MESSAGING = LookupService.lookup("tcp:de.willuhn.jameica.messaging.Plugin.connector.tcp") != null;
    
    return HAVE_MESSAGING;
  }

  @Override
  public boolean autoRegister()
  {
    return true;
  }

}


