/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Passport.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/04/19 22:05:52 $
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
import de.willuhn.jameica.hbci.rmi.hbci.PassportHandle;

/**
 * Bildet die Persistenz eines Passport ab.
 */
public interface Passport extends DBObject {

	/**
	 * Liefert den Namen des Passports.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
	
	/**
	 * Speichert den Namen des Passports.
   * @param name Name des Passports.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;

	/**
	 * Liefert den Typ dieses Passports.
   * @return Typ des Passports.
   * @throws RemoteException
   */
  public PassportType getPassportType() throws RemoteException;
	
	/**
	 * Speichert den Typ des Passports.
   * @param type Typ des Passports.
   * @throws RemoteException
   */
  public void setPassportType(PassportType type) throws RemoteException;

	/**
	 * Liefert das Passport-Handle.
   * @return Handle.
   * @throws RemoteException
   */
  public PassportHandle getHandle() throws RemoteException;

}


/**********************************************************************
 * $Log: Passport.java,v $
 * Revision 1.7  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/