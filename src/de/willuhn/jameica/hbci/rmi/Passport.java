/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Passport.java,v $
 * $Revision: 1.6 $
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

import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet einen Passport ab.
 * Ein Passport ist ein HBCI-Sicherheitsmedium - z.Bsp. Chipkarte (DDV).
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
   * Oeffnet den Passport.
   * @return Handler, der diese Verbindung repraesentiert.
   * @throws RemoteException muss geworfen werden, wenn die Initialisierung fehlschlaegt.
   * Die Exeption sollte einen sinnvollen Fehlertext enthalten. 
   */
  public HBCIHandler open() throws RemoteException;

	/**
	 * Schliesst den Passport.
   * @throws RemoteException
   */
  public void close() throws RemoteException;
  
  /**
   * Prueft, ob der Passport offen ist.
   * @return true, wenn er offen ist.
   * @throws RemoteException
   */
  public boolean isOpen() throws RemoteException;

	/**
	 * Liefert ein Array mit Konto-Objekten, die aus dem Medium gelesen wurden.
	 * Es wird niemals <code>null</code> zurueckgeliefert sondern hoechstens ein leeres Array.
	 * Hinweis: Die Konten-Objekte existieren nicht in der Datenbank.
	 * Um sie zu speichern muss fuer jedes die Methode <code>store()</code> aufgerufen werden.
   * @return Array mit Konten, die dieser Passport anbietet.
   * @throws RemoteException
   */
  public Konto[] getKonten() throws RemoteException;
  
	
}


/**********************************************************************
 * $Log: Passport.java,v $
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