/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/HBCIFactoryMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/12/27 22:47:52 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.messaging.Message;

/**
 * Wird von der HBCI-Factory verschickt, um ueber den Bearbeitungsstand zu informieren.
 */
public class HBCIFactoryMessage implements Message
{
  /**
   * Status-Codes.
   */
  public static enum Status
  {
    /**
     * Die HBCI-Factory hat ihre Arbeit beendet - egal ob erfolgreich oder nicht.
     */
    STOPPED,
  }

  private Status status = null;
  
  /**
   * ct.
   * @param status der Status.
   */
  public HBCIFactoryMessage(Status status)
  {
    this.status = status;
  }
  
  /**
   * Liefert den Status.
   * @return der Status.
   */
  public Status getStatus()
  {
    return this.status;
  }

}



/**********************************************************************
 * $Log: HBCIFactoryMessage.java,v $
 * Revision 1.1  2010/12/27 22:47:52  willuhn
 * @N BUGZILLA 964
 *
 **********************************************************************/