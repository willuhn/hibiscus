/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/UmsatzTyp.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/25 23:23:17 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface zur Einstufung von Umsaetzen in verschiedene Kategorien.
 */
public interface UmsatzTyp extends DBObject {

	/**
	 * Liefert den Namen des Umsatz-Typs.
   * @return Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Liefert das Umsatz-Feld, welches geprueft werden soll.
   * @return Feld, welches geprueft werden soll.
   * @throws RemoteException
   */
  public String getField() throws RemoteException;

	/**
	 * Liefert den regulaeren Ausdruck, mit dem gesucht werden soll.
   * @return regulaerer Ausdruck.
   * @throws RemoteException
   */
  public String getPattern() throws RemoteException;
	
	/**
	 * Speichert den Namen des Umsatz-Typs.
   * @param name Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
	
	/**
	 * Speichert das Umsatz-Feld, welches geprueft werden soll.
   * @param field Feld, welches geprueft werden soll. 
   * @throws RemoteException
   */
  public void setField(String field) throws RemoteException;
	
	/**
	 * Speichert den regulaeren Ausdruck, mit dem gesucht werden soll.
   * @param pattern regulaerer Ausdruck.
   * @throws RemoteException
   */
  public void setPattern(String pattern) throws RemoteException;

	/**
	 * Liefert eine Liste aller Umsaetze dieses Typs.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public DBIterator getUmsaetze() throws RemoteException;

}


/**********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/