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

/**
 * Basis-Interface, welches alle Serializer implementieren muessen.
 * @param <T> Typ, von und zu dem serialisiert wird.
 */
public interface Serializer<T>
{
  /**
   * Liefert einen Identifier für den Serializer.
   * @return der Identifier.
   */
  public String getIdentifier();
  
  /**
   * Serialisiert das uebergebene Objekt.
   * @param context das Context-Objekt.
   * In der Regel handelt es sich hier um die Bean, zu der das Attribut gehoert.
   * @param value das zu serialisierende Objekt.
   * @return das serialisierte Objekt.
   * @throws IOException
   */
  public String serialize(Object context, T value) throws IOException;
  
  /**
   * Deserialisiert den uebergebenen Text.
   * @param context das Context-Objekt.
   * In der Regel handelt es sich hier um die Bean, zu der das Attribut gehoert.
   * @param value der zu deserialisierende Text.
   * @return das deserialisierte Objekt.
   * @throws IOException
   */
  public T unserialize(Object context, String value) throws IOException;
}
