/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/hbci/Attic/PassportHandle.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/04/19 22:05:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi.hbci;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Bildet einen HBCI-Passport ab.
 * Ein Passport ist ein HBCI-Sicherheitsmedium - z.Bsp. Chipkarte (DDV).
 */
public interface PassportHandle extends Remote {

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
 * $Log: PassportHandle.java,v $
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/