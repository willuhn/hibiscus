/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Klassen, die dieses Interface implementieren, koennen mit Flags markiert werden.
 */
public interface Flaggable extends HibiscusDBObject
{
  /**
   * Liefert ein Bit-Feld mit Flags.
   * Ein Objekt kann mit verschiedenen Flags markiert
   * werden. Das kann zum Beispiel "deaktiviert" sein.
   * Damit fuer kuenftige weitere Flags nicht immer
   * ein neues Feld zur Datenbank hinzugefuegt werden
   * muss, verwenden wir hier ein Bitfeld. Damit koennen
   * mehrere Flags in einem Wert codiert werden.
   * @return Bit-Feld mit den Flags des Objektes.
   * @throws RemoteException
   */
  public int getFlags() throws RemoteException;
  
  /**
   * Speichert die Flags einen Objektes.
   * @param flags die Flags in Form eines Bit-Feldes.
   * @throws RemoteException
   */
  public void setFlags(int flags) throws RemoteException;
  
  /**
   * Prueft, ob das angegebene Flag vorhanden ist.
   * @param flag das zu pruefende Flag.
   * @return true, wenn es gesetzt ist.
   * @throws RemoteException
   */
  public boolean hasFlag(int flag) throws RemoteException;
}


/**********************************************************************
 * $Log: Flaggable.java,v $
 * Revision 1.3  2012/05/03 21:50:47  willuhn
 * @B BUGZILLA 1232 - Saldo des Kontos bei Offline-Konten nur bei neuen Umsaetzen uebernehmen - nicht beim Bearbeiten existierender
 *
 * Revision 1.2  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.1  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 **********************************************************************/