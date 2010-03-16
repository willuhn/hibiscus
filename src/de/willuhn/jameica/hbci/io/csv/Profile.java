/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/Profile.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/16 13:43:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean fuer ein Profil zum Import von CSV-Dateien.
 */
public class Profile implements Serializable
{
  private List<Column> columns = new ArrayList<Column>();
  private String separatorChar = ";";
  private String quotingChar   = "\"";
  private int skipLines        = 0;
  private String fileEncoding  = System.getProperty("file.encoding");
  
  private int version          = 0;

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
   * Liefert die Versionsnummer des Profils.
   * Wenn das Default-Profil des Formats eine hoehere Versionsnummer
   * liefert als die ggf. serialisierte Version im Benutzerverzeichnis,
   * dann wird die serialisierte Version ignoriert und stattdessen
   * wieder das Default-Profil verwendet. Andernfalls muesste der
   * User die XML-Datei in ~/.jameica/hibiscus/csv manuell loeschen,
   * damit eine ggf. aktualisierte Profil-Version verwendet wird.
   * @return version Versionsnummer des Profils.
   */
  public int getVersion()
  {
    return version;
  }

  /**
   * Speichert die Versionsnummer des Profils.
   * @param version version
   */
  public void setVersion(int version)
  {
    this.version = version;
  }

  
}



/**********************************************************************
 * $Log: Profile.java,v $
 * Revision 1.2  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
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