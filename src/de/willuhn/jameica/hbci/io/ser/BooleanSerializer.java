/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.ser;

import java.io.IOException;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;

/**
 * Implementierung eines Serializers fuer Boolean-Werte.
 */
@Lifecycle(Type.CONTEXT)
public class BooleanSerializer extends DefaultSerializer<Boolean>
{
  /**
   * Die ID.
   */
  public final static String ID = "boolean";
  
  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#getIdentifier()
   */
  @Override
  public String getIdentifier()
  {
    return ID;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  @Override
  public Boolean unserialize(Object context, String value) throws IOException
  {
    return Boolean.valueOf(value);
  }

}
