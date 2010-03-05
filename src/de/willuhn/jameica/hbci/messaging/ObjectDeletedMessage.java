/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/ObjectDeletedMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/05 15:24:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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