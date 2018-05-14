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
public interface KeyFormat
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
}


/**********************************************************************
 * $Log: KeyFormat.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.3  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.2  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.1  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 **********************************************************************/
