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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Serializers fuer Geld-Betraege.
 */
@Lifecycle(Type.CONTEXT)
public class ValueSerializer extends DefaultSerializer<Double>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Die ID.
   */
  public final static String ID = "value";
  
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
  public String serialize(Object context, Double value) throws IOException
  {
    if (value == null)
      return super.serialize(context, value);
    
    return HBCI.DECIMALFORMAT.format(value) + " " + HBCIProperties.CURRENCY_DEFAULT_DE;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  @Override
  public Double unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;
    
    try
    {
      // Wir ersetzen alles, was nicht Zahl, Komma oder Punkt ist.
      // Damit entfernen wir automatisch alle Waehrungskennzeichen
      value = value.replaceAll("[^0-9-,\\.]","");

      // Nix mehr zum Parsen uebrig?
      if (value.length() == 0)
        throw new Exception();
      
      // Wenn der Text jetzt Punkt UND Komma enthaelt, entfernen wir die Punkte
      if (value.indexOf(".") != -1 && value.indexOf(",") != -1)
        value = value.replaceAll("\\.","");
      
      // Wenn jetzt immer ein Punkt drin sind, muss es ein Komma sein
      if (value.indexOf(".") != -1)
        value = value.replaceFirst("\\.",",");
      
      // Wenn jetzt immer noch ein Punkt drin ist, sah der Text
      // vorher so aus: 123.456.000
      // Dann entfernen wir alle Punkte
      value = value.replaceAll("\\.","");
      
      return HBCI.DECIMALFORMAT.parse(value).doubleValue();
    }
    catch (Exception e)
    {
      Logger.error("unable to parse string " + value + " as double",e);
      throw new IOException(i18n.tr("Text \"{0}\" ist kein gültiger Betrag",value));
    }
  }

}
