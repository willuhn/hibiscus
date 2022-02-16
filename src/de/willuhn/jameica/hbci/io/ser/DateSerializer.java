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

  @Override
  public String serialize(Object context, Date value) throws IOException
  {
    return value == null ? super.serialize(context,value) : HBCI.DATEFORMAT.format(value);
  }

  @Override
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
      throw new IOException(i18n.tr("Text \"{0}\" ist kein g�ltiges Datum",value));
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