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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Implementierung eines Serializers fuer erweiterte Verwendungszwecke.
 */
public class ExtendedUsageSerializer extends DefaultSerializer<String[]>
{
  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  public String serialize(Object context, String[] value) throws IOException
  {
    if (value == null)
      return super.serialize(context, value);

    StringBuffer sb = new StringBuffer();
    for (String s:value)
    {
      sb.append(s);
      sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  public String[] unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;

    List<String> lines = new ArrayList<String>();
    
    // Checken, ob wir schon vorherige Zeilen haben
    if (context != null && (context instanceof String[]))
      lines.addAll(Arrays.asList((String[])context));
    
    // Es kann sein, dass die erweiterten Verwendungszwecke in einer
    // langen Zeile vorliegen (so exportiert es Hibiscus auch).
    // In dem Fall zerlegen wir sie in 27 Teichen lange Segmente und fuegen
    // sie einzeln hinzu
    if (value.length() > HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH)
    {
      // Sieht komisch aus, ich weiss. In Converter#HBCIUmsatz2HibiscusUmsatz wird
      // das auch verwendet - und dort steht, warum ;)
      value = value.replaceAll("(.{27})","$1--##--##");
      lines.addAll(Arrays.asList(value.split("--##--##")));
    }
    else
    {
      // andernfalls einfach hinten dran pappen
      lines.add(value);
    }
    
    return lines.toArray(new String[0]);

  }

}



/**********************************************************************
 * $Log: ExtendedUsageSerializer.java,v $
 * Revision 1.2  2011/06/12 22:10:06  willuhn
 * @B BUGZILLA 1053
 *
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
