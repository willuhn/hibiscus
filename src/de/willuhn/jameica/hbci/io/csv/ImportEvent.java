/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/ImportEvent.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/03/16 00:44:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import de.willuhn.util.ProgressMonitor;

/**
 * Event-Objekt.
 */
public class ImportEvent
{
  /**
   * Das zugehoerige Fachobjekt.
   */
  public Object data = null;
  
  /**
   * Optionales Context-Objekt des Imports.
   */
  public Object context = null;
  
  /**
   * Der Fortschrittsmonitor.
   */
  public ProgressMonitor monitor = null;
  
  /**
   * Flag, mit dem der Ausloeser des Events erkennen kann, ob der
   * Vorgang fortgesetzt werden soll oder nicht.
   */
  public boolean doit = true;
}



/**********************************************************************
 * $Log: ImportEvent.java,v $
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