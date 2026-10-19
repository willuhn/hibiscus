/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
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
 * Default-Implementierung eines Serializers.
 * @param <T> Typ, von und zu dem serialisiert wird.
 */
@Lifecycle(Type.CONTEXT)
public class DefaultSerializer<T> implements Serializer<T>
{
  /**
   * Die ID.
   */
  public final static String ID = "default";
  
  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#getIdentifier()
   */
  @Override
  public String getIdentifier()
  {
    return ID;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#serialize(java.lang.Object, java.lang.Object)
   */
  @Override
  public String serialize(Object context, T value) throws IOException
  {
    return value != null ? value.toString() : "";
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#unserialize(java.lang.Object, java.lang.String)
   */
  @Override
  public T unserialize(Object context, String value) throws IOException
  {
    if (value == null || (value instanceof String))
      return (T) value;
    throw new IOException("unable to unserialize this type, value: " + value);
  }

}
