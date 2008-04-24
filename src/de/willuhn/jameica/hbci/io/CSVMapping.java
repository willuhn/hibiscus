/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVMapping.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/04/24 11:37:21 $
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

import org.supercsv.prefs.CsvPreference;

import de.willuhn.jameica.system.Settings;

/**
 * Ein Container, der die Feldzuordnungen eines CSV-Imports speichert.
 */
public class CSVMapping
{
  private final static Settings SETTINGS = new Settings(CSVMapping.class); 
  
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
   * Liefert eine Kopie der Hashtable mit den Namen.
   * @return Hashtable mit den Namen.
   */
  public Hashtable getNames()
  {
    return (Hashtable) this.names.clone();
  }

  /**
   * Liest das Mapping aus der Config-Datei.
   */
  private synchronized void read()
  {
    mapping = new Hashtable();
    Enumeration e = names.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      int index = SETTINGS.getInt(type.getName() + "." + key,-1);
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
    store();
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
    store();
  }
  
  /**
   * Liefert das Spalten-Trennzeichen.
   * @return Spalten-Trennzeichen.
   */
  public String getSeparatorChar()
  {
    return SETTINGS.getString("separator.char",";");
  }
  
  /**
   * Speichert das Spalten-Trennzeichen.
   * @param sepChar Spalten-Trennzeichen.
   */
  public void setSeparatorChar(String sepChar)
  {
    SETTINGS.setAttribute("separator.char",(sepChar != null && sepChar.length() == 1) ? sepChar : ";");
  }
  
  /**
   * Liefert das Quoting-Zeichen fuer die Spalten.
   * @return Quoting-Zeichen.
   */
  public String getQuotingChar()
  {
    return SETTINGS.getString("quoting.char","\"");
  }
  
  /**
   * Speichert das Quoting-Zeichen fuer die Spalten.
   * @param quotingChar Quoting-Zeichen.
   */
  public void setQuotingChar(String quotingChar)
  {
    SETTINGS.setAttribute("quoting.char",(quotingChar != null && quotingChar.length() == 1) ? quotingChar : "");
  }
  
  /**
   * Prueft, ob die erste Zeile der Datei beim Einlesen uebersprungen werden soll.
   * Ist sinnvoll, wenn diese Zeile den Tabellenkopf enthaelt.
   * @return true, wenn die erste Zeile uebersprungen werden soll.
   */
  public boolean getSkipFirst()
  {
    return SETTINGS.getBoolean("skipfirst",false);
  }
  
  /**
   * Legt fest, ob die erste Zeile der Datei beim Einlesen uebersprungen werden soll.
   * Ist sinnvoll, wenn diese Zeile den Tabellenkopf enthaelt.
   * @param b true, wenn die erste Zeile uebersprungen werden soll.
   */
  public void setSkipFirst(boolean b)
  {
    SETTINGS.setAttribute("skipfirst",b);
  }
  
  /**
   * Liefert den Zeichensatz, der zum Einlesen der Datei verwendet werden soll.
   * Per Default wird das Plattform-Encoding zurueckgeliefert.
   * @return Zeichensatz.
   */
  public String getFileEncoding()
  {
    return SETTINGS.getString("file.encoding",System.getProperty("file.encoding"));
  }
  
  /**
   * Speichert den Zeichensatz, der zum Einlesen der Datei verwendet werden soll.
   * Per Default wird das Plattform-Encoding zurueckgeliefert.
   * @param encoding Zeichensatz.
   */
  public void setFileEncoding(String encoding)
  {
    SETTINGS.setAttribute("file.encoding",(encoding != null && encoding.length() > 0) ? encoding : null);
  }
  
  /**
   * Speichert das CSV-Mapping.
   */
  private synchronized void store()
  {
    Enumeration e = mapping.keys();
    while (e.hasMoreElements())
    {
      Integer index = (Integer) e.nextElement();
      String key = (String) mapping.get(index);
      SETTINGS.setAttribute(type.getName() + "." + key,index.intValue());
    }
  }
  
  /**
   * Liefert ein CsvPreference-Objekt basierend auf den aktuellen Einstellungen.
   * @return CsvPreference-Objekt.
   */
  public CsvPreference toCsvPreference()
  {
    String sep  = this.getSeparatorChar();
    String quot = this.getQuotingChar();

    CsvPreference prefs = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    if (sep != null && sep.length() == 1)   prefs.setDelimiterChar(sep.charAt(0));
    if (quot != null && quot.length() == 1) prefs.setQuoteChar(quot.charAt(0));
    
    return prefs;
  }
}


/*********************************************************************
 * $Log: CSVMapping.java,v $
 * Revision 1.5  2008/04/24 11:37:21  willuhn
 * @N BUGZILLA 304
 *
 * Revision 1.4  2006/08/21 23:15:00  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.3  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.2  2006/01/23 18:16:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/