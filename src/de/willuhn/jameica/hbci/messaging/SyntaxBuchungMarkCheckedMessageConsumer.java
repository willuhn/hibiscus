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

import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.logging.Logger;

/**
 * Empfaengt die Geprueft-Markierungen von SynTAX und synchronisiert den Status mit Hibiscus.
 */
public class SyntaxBuchungMarkCheckedMessageConsumer implements MessageConsumer
{
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[] {QueryMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    final QueryMessage m = (QueryMessage) message;
    final String state   = m.getName();
    final Object data    = m.getData();
    
    if (state == null || state.length() == 0 || data == null)
      return;
    
    final String id = data.toString();
    if (id == null || id.length() == 0)
      return;

    final boolean set = Boolean.valueOf(state);
    
    try
    {
      Umsatz u = Settings.getDBService().createObject(Umsatz.class,id);

      final int current = u.getFlags();
      final int flag = Umsatz.FLAG_CHECKED;
      if (set)
        u.setFlags(current | flag);
      else
        u.setFlags(current ^ flag);
      
      u.store();
    }
    catch (ObjectNotFoundException oe)
    {
      Logger.warn("unable to sync checked state, booking no longer exists: " + id);
    }
  }

  @Override
  public boolean autoRegister()
  {
    // Per plugin.xml registriert
    return false;
  }

}


