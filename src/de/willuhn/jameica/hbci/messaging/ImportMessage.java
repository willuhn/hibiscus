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

import de.willuhn.datasource.GenericObject;

/**
 * Diese Art von Nachricht wird verschickt, wenn ein Datensatz importiert wurde.
 * Um diese Nachrichten zu erhalten, kann man sich als MessageConsumer
 * in Jameica (Application.getMessagingFactory()) registrieren und diese
 * Arte von Nachrichten abonnieren.
 */
public class ImportMessage extends ObjectMessage
{

  /**
   * ct.
   * @param object
   */
  public ImportMessage(GenericObject object)
  {
    super(object);
  }
}

/*********************************************************************
 * $Log: ImportMessage.java,v $
 * Revision 1.2  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.1  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.1  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 **********************************************************************/