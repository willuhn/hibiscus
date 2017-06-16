/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import de.willuhn.jameica.messaging.Message;

/**
 * Wird zum Tracen der rohen HBCI-Nachrichten verwendet.
 */
public class HBCITraceMessage implements Message
{
  /**
   * Der Typ der Message.
   */
  public enum Type
  {
    /**
     * rohe HBCI-Message gesendet.
     */
    SEND,
    
    /**
     * rohe HBCI-Message empfangen.
     */
    RECV,
    
    /**
     * Freitext-Informationen zum Kontext.
     */
    INFO,
    
    /**
     * Identifier.
     */
    ID,
    
    /**
     * Session fuer die ID geschlossen.
     */
    CLOSE,
  }
  
  private Type type = null;
  private String data = null;
  
  /**
   * ct.
   * @param type
   * @param data
   */
  public HBCITraceMessage(Type type, String data)
  {
    this.type = type;
    this.data = data;
  }
  
  /**
   * Liefert den Payload der Message.
   * @return der Payload der Message.
   */
  public String getData()
  {
    return data;
  }
  
  /**
   * Liefert den Typ der Message.
   * @return der Typ der Message.
   */
  public Type getType()
  {
    return type;
  }

}


