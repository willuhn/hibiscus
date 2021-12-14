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

import de.willuhn.jameica.system.OperationCanceledException;


/**
 * Listener, den ein Format mitbringen kann, um Einfluss auf den Import-Vorgang
 * nehmen zu koennen.
 */
public class ImportListener
{
  /**
   * Wird aufgerufen, unmittelbar bevor das Objekt in der Datenbank gespeichert wird.
   * @param event das Import-Event.
   * Das Property "data" ist die zu speichernde Bean.
   * @throws OperationCanceledException wenn das Speichern des Objektes uebersprungen werden soll.
   */
  public void beforeStore(ImportEvent event) throws OperationCanceledException
  {
  }
  
  /**
   * Wird aufgerufen, nachdem alle Werte der Zeile deserialisiert, aber noch nicht
   * zur Bean hinzugefuegt wurden. Die Format-Implementierung kann hier - nachdem
   * alle Properties gelesen wurden, nochmal ein Postprocessing durchfuehren, bevor
   * die Werte gespeichert werden.
   * Das wird z.Bsp. gebraucht, wenn ein Property in der Bean aus mehreren CSV-Spalten
   * zusammengesetzt ist.
   * @param event das Import-Event.
   * Das Property "data" ist eine {@code Map<String,Object>} mit den Property-Namen als
   * Keys und den deserialisierten Property-Werten als Values.
   * @throws OperationCanceledException wenn das Objekt uebersprungen werden soll.
   */
  public void beforeSet(ImportEvent event) throws OperationCanceledException
  {
    
  }
}



/**********************************************************************
 * $Log: ImportListener.java,v $
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