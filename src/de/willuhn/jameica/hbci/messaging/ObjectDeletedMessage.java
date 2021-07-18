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

  /**
   * ct.
   * @param object
   */
  public ObjectDeletedMessage(GenericObject object)
  {
    super(object);
  }

}

/*********************************************************************
 * $Log: ObjectDeletedMessage.java,v $
 * Revision 1.1  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 **********************************************************************/