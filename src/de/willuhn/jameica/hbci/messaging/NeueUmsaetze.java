/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/NeueUmsaetze.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/01/30 18:25:33 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;

/**
 * Ueber die Klasse koennen die in der aktuellen Session
 * abgerufenen Umsaetze ermittelt werden.
 */
public class NeueUmsaetze implements MessageConsumer
{
  private static String first = null;

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
    return new Class[]{ImportMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist oder wir schon den ersten Umsatz haben,
    // ignorieren wir die folgenden
    if (first != null || message == null || !(message instanceof ImportMessage))
      return;
    
    GenericObject o = ((ImportMessage)message).getImportedObject();
    
    if (o == null || o.getID() == null || !(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    
    first = o.getID();
  }
  
  /**
   * Liefert eine Liste mit allen in der aktuellen Sitzung hinzugekommenen Umsaetzen.
   * @return Liste der neuen Umsaetze.
   * @throws RemoteException
   */
  public static GenericIterator getNeueUmsaetze() throws RemoteException
  {
    if (first == null)
      return PseudoIterator.fromArray(new Umsatz[0]);
    DBIterator list = Settings.getDBService().createList(Umsatz.class);
    list.addFilter("id >= " + first);
    list.setOrder("ORDER BY TONUMBER(valuta) desc, id desc");
    if (list.size() == 0)
      first = null; // Wenn nichts gefunden wurde, resetten wir uns
    return list;
  }

}


/*********************************************************************
 * $Log: NeueUmsaetze.java,v $
 * Revision 1.1  2007/01/30 18:25:33  willuhn
 * @N Bug 302
 *
 **********************************************************************/