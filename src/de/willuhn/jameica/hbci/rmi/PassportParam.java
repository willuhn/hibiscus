/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/PassportParam.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/12 00:38:40 $
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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet einen Initialisierungsparameter fuer einen Passport ab.
 * Wir wollen ja, dass z,Bsp. eine Chipkarte pro Bankverbindung
 * konfiguriert werden kann. Daher haengen die Passport-Parameter
 * nicht an dem Passport selbst dran sondern am zugehoerigen Konto.
 * Uebder dieses kann dann der ausgewaehlte Passport geladen werden.
 */
public interface PassportParam extends DBObject {

	
	/**
	 * Liefert den Namen des Parameters.
   * @return Name des Parameters.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Liefert den Wert des Parameters.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  public String getValue() throws RemoteException;

	/**
	 * Lifert das Konto, zu dem der Parameter gehoert.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;

	/**
	 * Speichert den Namen des Parameters.
   * @param name Name des Parameters.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;

	/**
	 * Speichert den Wert des Parameters.
   * @param value Wert des Parameters.
   * @throws RemoteException
   */
  public void setValue(String value) throws RemoteException;

	/**
	 * Konto des Parameters.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;	
}


/**********************************************************************
 * $Log: PassportParam.java,v $
 * Revision 1.2  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/