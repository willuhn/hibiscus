/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Duplicatable.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/04/22 12:42:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Basis-Interface fuer Objekte, die duplizierbar sind.
 * Eine Ueberweisung kann damit zum Beispiel dupliziert werden,
 * um eine neue Ueberweisung mit den gleichen Eigenschaften zu erzeugen.
 * @param <T> Duplizierter Typ.
 */
public interface Duplicatable<T>
{
  /**
   * Dupliziert das Objekt.
   * @return neues Objekt mit den gleichen Eigenschaften.
   * @throws RemoteException
   */
  public T duplicate() throws RemoteException;

}

/*****************************************************************************
 * $Log: Duplicatable.java,v $
 * Revision 1.2  2010/04/22 12:42:02  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.1  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
*****************************************************************************/