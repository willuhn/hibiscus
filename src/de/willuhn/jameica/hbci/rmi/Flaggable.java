/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Flaggable.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/10/18 09:28:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
}


/**********************************************************************
 * $Log: Flaggable.java,v $
 * Revision 1.2  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.1  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 **********************************************************************/