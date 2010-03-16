/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/ser/Serializer.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 00:44:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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



/**********************************************************************
 * $Log: Serializer.java,v $
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