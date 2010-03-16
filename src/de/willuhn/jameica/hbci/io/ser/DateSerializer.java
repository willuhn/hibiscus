/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/ser/DateSerializer.java,v $
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Serializer fuer Datums-Werte.
 */
public class DateSerializer extends DefaultSerializer<Date>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private DateFormat SHORTDATEFORMAT = new SimpleDateFormat("dd.MM.yy");

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  public String serialize(Object context, Date value) throws IOException
  {
    return value == null ? super.serialize(context,value) : HBCI.DATEFORMAT.format(value);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  public Date unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;
    
    try
    {
      if (value == null || value.length() == 0)
        return null;
      if (value.matches("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2}"))
        return SHORTDATEFORMAT.parse(value);
      return HBCI.DATEFORMAT.parse(value);
    }
    catch (Exception e)
    {
      Logger.error("unable to parse string " + value + " as date",e);
      throw new IOException(i18n.tr("Text \"{0}\" ist kein gültiges Datum",value));
    }
  }

}



/**********************************************************************
 * $Log: DateSerializer.java,v $
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