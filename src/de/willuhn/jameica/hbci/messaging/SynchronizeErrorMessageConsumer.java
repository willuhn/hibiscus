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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Wird benachrichtigt, wenn es bei der Synchronisierung zu einem Fehler kam.
 * Der User kann dann selbst entscheiden, ob er fortsetzt oder nicht.
 */
public class SynchronizeErrorMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    
    // Im Server-Mode fragen wir den User nicht, weil es dann ziemlich sicher
    // der Payment-Server ist und da wohl keine Interaktion via Shell moeglich ist.
    // Ob der Payment-Server diese Message ebenfalls abonniert hat, ist seine Sache.
    if (Application.inServerMode())
      return;
    
    // User fragen
    Boolean cont = Application.getCallback().askUser(i18n.tr("Synchronisierung fehlgeschlagen.\nM�chten Sie den Vorgang dennoch fortsetzen?"));
    msg.setData(cont);
  }

  @Override
  public boolean autoRegister()
  {
    return false; // passiert via plugin.xml
  }
}


