/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passport/PassportHandle.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/05 22:14:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passport;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.kapott.hbci.manager.HBCIHandler;

import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Bildet die direkte Verbindung zum HBCIHandler aus HBCI4Java ab.
 */
public interface PassportHandle extends Remote {

  /**
   * Oeffnet den Passport und liefert den HBCIHandler zrueck.
   * Diese Funktion wird von der HBCIFactory bei der Ausfuehrung von
   * HBCIJobs verwendet.<br>
   * <b>Hinweis:</b>Die Funktion hat den HBCIHandler fix und fertig
   * vorkonfiguriert auszuliefern. Die HBCIFactory verwendet ihn direkt
   * ohne weitere Parameter zu setzen.
   * @return Handler, der diese Verbindung repraesentiert.
   * @throws RemoteException muss geworfen werden, wenn die Initialisierung fehlschlaegt.
   * Die Exeption sollte einen sinnvollen Fehlertext enthalten. 
   */
  public HBCIHandler open() throws RemoteException;

	/**
	 * Schliesst den Passport.
	 * Die Funktion wird von der HBCIFactory nach Durchfuehrung
	 * der HBCI-Jobs ausgefuehrt. In dieser Funktion sollte der
	 * HBCIHandler geschlossen werden.
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
	 * Es wird niemals <code>null</code> zurueckgeliefert sondern hoechstens ein leeres Array.<br>
	 * Hinweis: Die Konten-Objekte duerfen nicht in der Datenbank gespeichert
	 * werden. Diese Entscheidung bleibt dem Anwender ueberlassen.
	 * Da der HBCIHandler von HBCI4Java ja Konto-Objekte vom Typ
	 * <i>org.kapott.hbci.structures.Konto</i> liefert, koennen diese
	 * via <i>de.willuhn.jameica.hbci.server.Converter.HBCIKonto2JameicaKonto(Konto)</i>
	 * in Fachobjekte des HBCI-Plugins konvertiert werden. 
   * @return Array mit Konten, die dieser Passport anbietet.
   * @throws RemoteException
   */
  public Konto[] getKonten() throws RemoteException;
  
	
}


/**********************************************************************
 * $Log: PassportHandle.java,v $
 * Revision 1.1  2004/05/05 22:14:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.1  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 **********************************************************************/