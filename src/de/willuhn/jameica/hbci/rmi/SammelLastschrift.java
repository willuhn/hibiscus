/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelLastschrift.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/02/28 16:28:24 $
 * $Author: web0 $
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
 * Interface fuer Sammellastschriften.
 */
public interface SammelLastschrift extends DBObject, Terminable
{
	/**
	 * Liefert eine Liste der Buchungen fuer diese Sammellastschrift.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public DBIterator getBuchungen() throws RemoteException;

	/**
	 * Liefert das Konto, ueber das die Sammellastschrift gutgeschrieben wird.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;

	/**
	 * Speichert das Konto, ueber das die Sammellastschrift gutgeschrieben werden soll.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;

  /**
   * Liefert eine Bezeichnung der Sammellastschrift.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

  /**
   * Speichert die Bezeichnung.
   * @param bezeichnung
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;
}


/**********************************************************************
 * $Log: SammelLastschrift.java,v $
 * Revision 1.2  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 **********************************************************************/