/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/PassportParam.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/27 01:10:18 $
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
 * Da die verschiedenen bei HBCI moeglichen Passports ja ganz unterschiedliche
 * Parameter benoetigen, sind diese in einer extra Tabelle aus Name:Wert
 * Paaren gespeichert. Diese Klasse wird ausschliesslich von den
 * Passports selbst zum Speichern Ihren Einstellungen verwendet.
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
	 * Liefert den Passport, zu dem der Parameter gehoert.
   * @return Passport.
   * @throws RemoteException
   */
  public Passport getPassport() throws RemoteException;

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
	 * Speichert den Passport, zu dem der Parameter gehoert.
   * @param passport der Passport.
   * @throws RemoteException
   */
  public void setPassport(Passport passport) throws RemoteException;	
}


/**********************************************************************
 * $Log: PassportParam.java,v $
 * Revision 1.3  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.2  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/