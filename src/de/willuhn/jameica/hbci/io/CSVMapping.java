/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVMapping.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/23 18:13:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.util.Enumeration;
import java.util.Hashtable;

import de.willuhn.jameica.system.Settings;

/**
 * Ein Container, der die Feldzuordnungen eines CSV-Imports speichert.
 */
public class CSVMapping
{

  private Settings settings = new Settings(CSVMapping.class); 
  
  private Class type         = null;
  private Hashtable names    = null;
  
  private Hashtable mapping  = null;
  
  /**
   * @param type
   * @param names
   */
  public CSVMapping(Class type, Hashtable names)
  {
    this.type  = type;
    this.names = names;
    read();
  }

  /**
   * Liest das Mapping aus der Config-Datei.
   */
  private void read()
  {
    mapping = new Hashtable();
    Enumeration e = names.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      int index = settings.getInt(type.getName() + "." + key,-1);
      if (index == -1)
        continue;
      mapping.put(new Integer(index),key);
    }
  }
  
  /**
   * Speichert ein einzelnes Mapping.
   * @param index Spaltennummer.
   * @param key zugeordnetes Attribut.
   */
  public void set(int index, String key)
  {
    mapping.put(new Integer(index),key);
  }
  
  /**
   * Liefert die Zuordnung zu diesem Index oder null, wenn keiner definiert ist.
   * @param index Spaltennummer.
   * @return zugeordnetes Attribut.
   */
  public String get(int index)
  {
    return (String) mapping.get(new Integer(index));
  }

  /**
   * Entfernt die Zuordnung zu dieser Spalte.
   * @param index zu entfernende Zuordnung.
   */
  public void remove(int index)
  {
    mapping.remove(new Integer(index));
  }
  
  /**
   * Speichert das CSV-Mapping.
   */
  public void store()
  {
    Enumeration e = mapping.keys();
    while (e.hasMoreElements())
    {
      Integer index = (Integer) e.nextElement();
      String key = (String) mapping.get(index);
      settings.setAttribute(type.getName() + "." + key,index.intValue());
    }
  }
}


/*********************************************************************
 * $Log: CSVMapping.java,v $
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/