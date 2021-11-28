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
import de.willuhn.jameica.messaging.Message;

/**
 * Nachricht, die ein Fachobjekt betrifft.
 */
public class ObjectMessage implements Message
{
  private final GenericObject object;

  /**
   * ct.
   * @param object das Objekt.
   */
  public ObjectMessage(GenericObject object)
  {
    this.object = object;
  }
  
  /**
   * Liefert das betreffende Objekt.
   * @return das Objekt.
   */
  public GenericObject getObject()
  {
    return this.object;
  }

}


/*********************************************************************
 * $Log: ObjectMessage.java,v $
 * Revision 1.1  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 **********************************************************************/