/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.Session;

/**
 * Basis-Interface aller Exporter.
 * Alle Klassen, die dieses Interface implementieren, werden automatisch
 * von Hibiscus erkannt und dem Benutzer als Export-Moeglichkeit angeboten
 * insofern sie einen parameterlosen Konstruktor mit dem Modifier "public"
 * besitzen (Java-Bean-Konvention).
 */
public interface Exporter extends IO
{
  /**
   * Eine Session fuer zusaetzliche Parameter.
   */
  public final static Session SESSION = new Session();
  
  /**
   * Exportiert die genannten Objekte in den angegebenen OutputStream.
   * @param objects die zu exportierenden Objekte.
   * @param format das vom User ausgewaehlte Export-Format.
   * @param os der Ziel-Ausgabe-Stream.
   * Der Exporter muss den OutputStream selbst schliessen!
   * @param monitor ein Monitor, an den der Exporter Ausgaben ueber seinen
   * Bearbeitungszustand ausgeben kann.
   * @throws RemoteException
   * @throws ApplicationException 
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException;
  
  /**
   * Liefert true, wenn der Exporter die angegebene Extension unterstuetzt.
   * Hintergrund: Im Export-Dialog koennen verschiedene Optionen (wie etwa "Spalte Saldo ausblenden") angezeigt
   * werden. Manche Export-Formate unterstuetzen diese Option jedoch gar nicht, sodass sie ignoriert werden wuerde.
   * Aus dem Grund kann der Exporter selbst mitteilen, ob er die angegebene Option unterstuetzt.
   * Unterstuetzt er sie nicht, wir die Option automatisch deaktiviert.
   * @param ext der Name der Extension.
   * @return true, wenn er die Extension unterstuetzt.
   */
  public boolean suppportsExtension(String ext);
}
