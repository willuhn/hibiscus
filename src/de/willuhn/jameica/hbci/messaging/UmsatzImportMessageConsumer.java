/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/Attic/UmsatzImportMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/12/29 15:26:56 $
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
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.util.Session;

/**
 * Dieser Consumer wird ueber das Eintreffen neuer Umsaetze
 * benachrichtigt und haelt diese fuer einige Zeit vor.
 */
public class UmsatzImportMessageConsumer implements MessageConsumer
{
  private final static Session umsaetze = new Session();

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
    return new Class[]{Umsatz.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof ImportMessage))
      return;
    ImportMessage im = (ImportMessage) message;
    GenericObject o = im.getImportedObject();
    if (o == null || !(o instanceof Umsatz))
      return;
    umsaetze.put(o.getID(),o);
  }
  
  /**
   * Liefert eine Liste neuer Umsaetze.
   * Das sind jene, die innerhalb der letzten halben Stunde eingetroffen sind.
   * @return Liste neuer Umsaetze.
   * @throws RemoteException
   */
  public final static GenericIterator getNewUmsaetze() throws RemoteException
  {
    if (umsaetze.size() == 0)
      return PseudoIterator.fromArray(new Umsatz[0]);

    // TODO Die sollten nach ID sortiert ausgegeben werden
    return null;
  }
}


/*********************************************************************
 * $Log: UmsatzImportMessageConsumer.java,v $
 * Revision 1.1  2006/12/29 15:26:56  willuhn
 * @N ImportMessageConsumer
 *
 **********************************************************************/