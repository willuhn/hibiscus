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
import java.util.Date;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.util.DateParser;

/**
 * Serializer fuer Datums-Werte.
 */
@Lifecycle(Type.CONTEXT)
public class DateSerializer extends DefaultSerializer<Date>
{
  /**
   * Die ID.
   */
  public final static String ID = "date";
  
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
  public String serialize(Object context, Date value) throws IOException
  {
    return value == null ? super.serialize(context,value) : HBCI.DATEFORMAT.format(value);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  @Override
  public Date unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;
    
    return DateParser.parse(value);
  }

}
