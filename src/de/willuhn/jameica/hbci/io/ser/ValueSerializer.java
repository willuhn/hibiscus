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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Serializers fuer Geld-Betraege.
 */
public class ValueSerializer extends DefaultSerializer<Double>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  public String serialize(Object context, Double value) throws IOException
  {
    if (value == null)
      return super.serialize(context, value);

    return HBCI.DECIMALFORMAT.format(value) + " " + HBCIProperties.CURRENCY_DEFAULT_DE;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
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

/**********************************************************************
 * $Log: ValueSerializer.java,v $
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