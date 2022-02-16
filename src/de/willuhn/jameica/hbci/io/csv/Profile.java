/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.prefs.CsvPreference;
import org.supercsv.prefs.CsvPreference.Builder;

/**
 * Bean fuer ein Profil zum Import von CSV-Dateien.
 */
public class Profile implements Serializable, Comparable
{
  private String name          = null;
  private boolean system       = false;
  private List<Column> columns = new ArrayList<Column>();
  private String separatorChar = ";";
  private String quotingChar   = "\"";
  private int skipLines        = 0;
  private String fileEncoding  = System.getProperty("file.encoding");
  
  /**
   * Liefert die Liste der Spalten fuer das Profil.
   * @return columns Liste der Spalten fuer das Profil.
   */
  public List<Column> getColumns()
  {
    return columns;
  }

  /**
   * Speichert die Liste der Spalten fuer das Profil.
   * @param columns Liste der Spalten.
   */
  public void setColumns(List<Column> columns)
  {
    this.columns = columns;
  }
  
  /**
   * Liefert das Spalten-Trennzeichen.
   * @return Spalten-Trennzeichen.
   */
  public String getSeparatorChar()
  {
    return this.separatorChar;
  }
  
  /**
   * Speichert das Spalten-Trennzeichen.
   * @param s Spalten-Trennzeichen.
   */
  public void setSeparatorChar(String s)
  {
    this.separatorChar = (s != null && s.length() == 1) ? s : ";";
  }
  
  /**
   * Liefert das Quoting-Zeichen fuer die Spalten.
   * @return Quoting-Zeichen.
   */
  public String getQuotingChar()
  {
    return this.quotingChar;
  }
  
  /**
   * Speichert das Quoting-Zeichen fuer die Spalten.
   * @param s Quoting-Zeichen.
   */
  public void setQuotingChar(String s)
  {
    this.quotingChar = (s != null && s.length() == 1) ? s : "";
  }
  
  /**
   * Liefert die Anzahl der zu ueberspringenden Zeilen.
   * Damit koennen ggf. vorhandene Ueberschriften uebersprungen werden.
   * @return Anzahl der zu ueberspringenden Zeilen.
   */
  public int getSkipLines()
  {
    return this.skipLines;
  }
  
  /**
   * Legt fest, wieviele Zeilen am Anfang uebersprungen werden sollen.
   * @param i Anzahl der zu ueberspringen Zeilen.
   */
  public void setSkipLines(int i)
  {
    if (i >= 0)
      this.skipLines = i;
  }
  
  /**
   * Liefert den Zeichensatz, der zum Einlesen der Datei verwendet werden soll.
   * Per Default wird das Plattform-Encoding zurueckgeliefert.
   * @return Zeichensatz.
   */
  public String getFileEncoding()
  {
    return this.fileEncoding;
  }
  
  /**
   * Speichert den Zeichensatz, der zum Einlesen der Datei verwendet werden soll.
   * Per Default wird das Plattform-Encoding zurueckgeliefert.
   * @param s Zeichensatz.
   */
  public void setFileEncoding(String s)
  {
    if (s != null && s.length() > 0)
      this.fileEncoding = s;
  }

  /**
   * Liefert den Namen des Profils.
   * @return der Name des Profils.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Speichert den Namen des Profils.
   * @param name der Name des Profils.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * Liefert true, wenn es sich um ein System-Profil handelt.
   * @return system true, wenn es sich um ein System-Profil handelt.
   */
  public boolean isSystem()
  {
    return system;
  }
  
  /**
   * Legt fest, ob es sich um ein System-Profil handelt.
   * @param system true, wenn es sich um ein System-Profil handelt.
   */
  public void setSystem(boolean system)
  {
    this.system = system;
  }
  
  /**
   * Erzeugt die passenden CSV-Preferences.
   * @return die passenden CSV-Preferences.
   */
  public CsvPreference createCsvPreference()
  {
    CsvPreference prefs = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    
    int sc  = prefs.getDelimiterChar();
    char qc = prefs.getQuoteChar();
    
    String sep = this.getSeparatorChar();
    String quo = this.getQuotingChar();
    
    if (sep != null && sep.length() == 1) sc = sep.charAt(0);
    if (quo != null && quo.length() == 1) qc = quo.charAt(0);
    
    Builder builder = new CsvPreference.Builder(qc,sc,prefs.getEndOfLineSymbols());
    return builder.build();
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Profile))
      return false;
    
    Profile other = (Profile) obj;
    return this.getName() != null && this.getName().equals(other.getName());
  }
  
  @Override
  public int compareTo(Object o)
  {
    // System-Profil immer vorn
    if (this.isSystem())
      return -1;
    
    Profile other = (Profile) o;
    if (other.isSystem())
      return 1;
    
    if (this.getName() == null)
      return -1;
    
    // Rest alphabetisch
    return this.getName().compareTo(other.getName());
  }
  
  @Override
  public String toString()
  {
    return this.getName() + ", encoding: " + this.getFileEncoding() + ", separator: " + this.getSeparatorChar() + ", quoting char: " + this.getQuotingChar() + ", skip lines: " + this.getSkipLines();
  }
}
