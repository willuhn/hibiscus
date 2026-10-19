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

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCIProperties;

/**
 * Implementierung eines Serializers fuer erweiterte Verwendungszwecke.
 */
@Lifecycle(Type.CONTEXT)
public class ExtendedUsageSerializer extends DefaultSerializer<String[]>
{
  /**
   * Die ID.
   */
  public final static String ID = "extendedusage";

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#getIdentifier()
   */
  @Override
  public String getIdentifier()
  {
    return ID;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  @Override
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

  @Override
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
