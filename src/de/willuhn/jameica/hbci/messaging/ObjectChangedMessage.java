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
 * Kann versendet werden, wenn sich ein Objekt geaendert hat.
 */
public class ObjectChangedMessage extends ObjectMessage
{

  /**
   * ct.
   * @param object
   */
  public ObjectChangedMessage(GenericObject object)
  {
    super(object);
  }

}


/*********************************************************************
 * $Log: ObjectChangedMessage.java,v $
 * Revision 1.1  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 **********************************************************************/