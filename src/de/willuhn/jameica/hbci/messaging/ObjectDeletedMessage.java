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
 * Kann versendet werden, wenn ein Objekt geloescht wurde.
 */
public class ObjectDeletedMessage extends ObjectMessage
{
  private String id = null;

  /**
   * ct.
   * @param object
   * @param id die ID, die das Objekt hatte.
   */
  public ObjectDeletedMessage(GenericObject object, String id)
  {
    super(object);
    this.id = id;
  }
  
  /**
   * Liefert die ID, die das gelöschte Objekt hatte.
   * @return die ID, die das gelöschte Objekt hatte.
   */
  public String getID()
  {
    return this.id;
  }

}
