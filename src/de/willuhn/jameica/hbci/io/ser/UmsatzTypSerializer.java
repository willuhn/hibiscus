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
import java.util.HashMap;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Serializers fuer Umsatz-Kategorien.
 */
public class UmsatzTypSerializer extends DefaultSerializer<UmsatzTyp>
{
  private Map<String,UmsatzTyp> cache = null;

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  public String serialize(Object context, UmsatzTyp value) throws IOException
  {
    if (value == null)
      return super.serialize(context,value);

    return value.getName();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  public UmsatzTyp unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;

    try
    {
      if (cache == null)
      {
        cache = new HashMap<String,UmsatzTyp>();
        DBIterator kategorien = Settings.getDBService().createList(UmsatzTyp.class);
        while (kategorien.hasNext())
        {
          UmsatzTyp t = (UmsatzTyp) kategorien.next();
          cache.put(t.getName().toLowerCase(),t);
        }
      }

      UmsatzTyp t = (UmsatzTyp) cache.get(value.toLowerCase());
      if (t != null)
        return t;

      // Nicht gefunden. Also neu anlegen
      Logger.info("auto-creating category " + value);
      t = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
      t.setName(value);
      t.setTyp(UmsatzTyp.TYP_EGAL);
      t.store();
      cache.put(value.toLowerCase(),t);
      return t;
    }
    catch (Exception e)
    {
      Logger.error("error while auto-creating category: " + value,e);
    }
    return null;
  }

}

/**********************************************************************
 * $Log: UmsatzTypSerializer.java,v $
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