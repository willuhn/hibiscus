/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/ImportMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/06 21:37:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.messaging.Message;

/**
 * Diese Art von Nachricht wird verschickt, wenn ein Datensatz importiert wurde.
 * Um diese Nachrichten zu erhalten, kann man sich als MessageConsumer
 * in Jameica (Application.getMessagingFactory()) registrieren und diese
 * Arte von Nachrichten abonnieren.
 */
public interface ImportMessage extends Message
{
  /**
   * Liefert das gerade importierte Objekt.
   * @return das importierte Objekt.
   * @throws RemoteException
   */
  public GenericObject getImportedObject() throws RemoteException;
}


/*********************************************************************
 * $Log: ImportMessage.java,v $
 * Revision 1.1  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 **********************************************************************/