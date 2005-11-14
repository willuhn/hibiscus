/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/UmsatzTyp.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/11/14 23:47:21 $
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
import de.willuhn.jameica.hbci.rmi.filter.Pattern;

/**
 * Interface zur Einstufung von Umsaetzen in verschiedene Kategorien.
 */
public interface UmsatzTyp extends DBObject, Pattern
{

	/**
	 * Liefert den Namen des Umsatz-Typs.
   * @return Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Speichert den Namen des Umsatz-Typs.
   * @param name Name des Umsatz-Typs.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
	
	/**
	 * Liefert eine Liste der Umsatz-Zuordnungen fuer diesen Umsatz.
   * @return Umsatz-Liste.
   * @throws RemoteException
   */
  public DBIterator getUmsatzZuordnungen() throws RemoteException;

}


/**********************************************************************
 * $Log: UmsatzTyp.java,v $
 * Revision 1.2  2005/11/14 23:47:21  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/