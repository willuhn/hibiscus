/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

import java.io.File;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;


/**
 * Interface fuer die unterstuetzten Schluessel-Formate.
 * Implementierungen muessen der Bean-Spezifikation entsprechen,
 * also einen parameterlosen Konstruktor mit dem Modifier public besitzen.
 */
public interface KeyFormat extends Comparable<KeyFormat>
{
  /**
   * Konstante fuer das Feature "Schluessel importieren".
   */
  public final static int FEATURE_IMPORT = 1;
  
  /**
   * Konstante fuer das Feature "Schluessel erstellen".
   */
  public final static int FEATURE_CREATE = 2;
  
  /**
   * Liefert einen sprechenden Namen des Formats.
   * @return Sprechender Name.
   */
  public String getName();
  
  /**
   * Importiert einen Schluessel.
   * Die Funktion soll nur ein RDHKey-Objekt erzeugen. Die
   * Registrierung des Schluessels in Hibiscus uebernimmt der Aufrufer.
   * @param file die Schluesseldatei.
   * @return der importierte Schluessel.
   * @throws ApplicationException Fehler.
   * @throws OperationCanceledException wenn der Import abgebrochen werden soll.
   */
  public RDHKey importKey(File file) throws ApplicationException, OperationCanceledException;
  
  /**
   * Erzeugt einen neuen Schluessel.
   * @param file Ziel-Datei, in der der Schluessel erzeugt werden soll.
   * @return der neue Schluessel.
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public RDHKey createKey(File file) throws ApplicationException, OperationCanceledException;
  
  /**
   * Prueft, ob das Format das angegebene Feature unterstuetzt.
   * @param feature das geforderte Feature.
   * @return true, wenn es unterstuetzt wird.
   */
  public boolean hasFeature(int feature);
  
  /**
   * Laedt einen Schluessel.
   * @param key der Schluessel.
   * @return HBCIPassport-Instanz des Schluessels.
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public HBCIPassport load(RDHKey key) throws ApplicationException, OperationCanceledException;
  
  /**
   * Liefert einen numerischen Wert fuer die Sortierung.
   * @return numerischer Wert fuer die Sortierung.
   * Je hoeher der Wert ist, desto weiter hinten ist er einsortiert.
   */
  public int getOrder();
}
