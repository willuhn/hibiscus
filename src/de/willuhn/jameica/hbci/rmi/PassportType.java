/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/PassportType.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/04/27 22:23:56 $
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
 * Dieses Objekt repraesentiert eine Liste der unterstuetzten Passport-Typen.
 */
public interface PassportType extends DBObject {

	/**
	 * Liefert den Namen des Passports.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

	/**
	 * Liefert den Namen der Java-Klasse, die diesen Passport implementiert.
   * @return Name der Klasse.
   * @throws RemoteException
   */
  public String getImplementor() throws RemoteException;

	/**
	 * Liefert den Namen der Java-Klasse, die den Dialog zur Konfiguration darstellt.
	 * Diese Klasse muss von <code>AbstractView</code> abgeleitet sein.
   * @return Klasse, die den Konfig-Dialog anzeigt.
   * @throws RemoteException
   */
  public String getAbstractView() throws RemoteException;
	
	/**
	 * Controller, der fuer den oben genannten Konfig-Dialog zustaendig ist.
	 * Diese Klasse muss von <code>AbstractControl</code> abgeleitet sein.
   * @return Controller, der fuer obigen Dialog zustaendig ist.
   * @throws RemoteException
   */
  public String getController() throws RemoteException;
}


/**********************************************************************
 * $Log: PassportType.java,v $
 * Revision 1.2  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/