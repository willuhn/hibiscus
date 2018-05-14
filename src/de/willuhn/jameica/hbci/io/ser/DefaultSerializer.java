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

/**
 * Default-Implementierung eines Serializers.
 * @param <T> Typ, von und zu dem serialisiert wird.
 */
public class DefaultSerializer<T> implements Serializer<T>
{
  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#serialize(java.lang.Object, java.lang.Object)
   */
  public String serialize(Object context, T value) throws IOException
  {
    return value != null ? value.toString() : "";
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.Serializer#unserialize(java.lang.Object, java.lang.String)
   */
  public T unserialize(Object context, String value) throws IOException
  {
    if (value == null || (value instanceof String))
      return (T) value;
    throw new IOException("unable to unserialize this type, value: " + value);
  }

}



/**********************************************************************
 * $Log: DefaultSerializer.java,v $
 * Revision 1.1  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/