/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/PassportParam.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/11 00:11:20 $
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

	// Parameter fuer den Port (meist 0)
	public final static String DDV_PORT 		= "ddv.port";
	public final static String[] DDV_PORTS = new String[] {"COM1","COM2","COM3","COM4","USB"};


	// Parameter fuer den Index (normalerweise 0)
	public final static String DDV_CTNUMBER = "ddv.ctnumber";

	// Parameter ober Biometrie verwendet wird (meist 0)
	public final static String DDV_USEBIO	  = "ddv.usebio";

	// Parameter ob die Tastatur zur Pin-Eingabe verwendet werden soll
	public final static String DDV_SOFTPIN  = "ddv.softpin";

	// Parameter fuer den Index des HBCI-Zugangs (meist 1)
	public final static String DDV_ENTRYIDX = "ddv.entryidx";
	
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
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/