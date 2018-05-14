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
 * CSV-Format fuer einen Datentyp in Hibiscus.
 * @param <T> Der Typ des korrespondierenden Objektes in Hibiscus.
 */
public interface Format<T>
{
  /**
   * Liefert den Typ des Fachobjektes, der mit diesem Format importiert werden kann.
   * @return Typ des Fachobjektes.
   */
  public Class<T> getType();
  
  /**
   * Liefert das Default-Profil fuer den Import.
   * @return das Default-Profil.
   */
  public Profile getDefaultProfile();

  /**
   * Liefert einen optionalen Import-Listener.
   * @return optionaler Import-Listener.
   */
  public ImportListener getImportListener();
}



/**********************************************************************
 * $Log: Format.java,v $
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