/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/IOFormat.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/23 23:07:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;


/**
 * Dieses Interface kapselt die Datei-Formate.
 * Jeder Importer oder Exporter unterstuetzt ein oder mehrere
 * Dateiformate. Ueber
 * <code>de.willuhn.jameica.hbci.io.IO#getIOFormats(Class type)</code>
 * kann ein Importer/Exporter abgefragt werden, welche Formate
 * er unterstuetzt.
 */
public interface IOFormat
{
  /**
   * Liefert einen sprechenden Namen fuer das Datei-Format.
   * Zum Beispiel &quotCSV-Datei&quot;
   * @return Sprechender Name des Datei-Formats.
   */
  public String getName();

  /**
   * Liefert die Datei-Endungen des Formats.
   * Zum Beispiel "*.csv" oder "*.txt".
   * @return Datei-Endung.
   */
  public String[] getFileExtensions();
}


/*********************************************************************
 * $Log: IOFormat.java,v $
 * Revision 1.2  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.1  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 **********************************************************************/