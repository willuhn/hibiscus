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
}



/**********************************************************************
 * $Log: ImportEvent.java,v $
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