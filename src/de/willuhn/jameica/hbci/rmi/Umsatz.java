/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Umsatz.java,v $
 * $Revision: 1.1 $
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
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet eine Zeile in den Kontoauszuegen ab.
 */
public interface Umsatz extends DBObject {

	/**
	 * Liefert ihr Konto, auf welches sich diese Umsaetze beziehen.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
	
	/**
	 * Liefert den Empfaenger der Zahlung. Bei Haben-Buchungen
	 * liefert die Funktion <code>null</code> da sie ja selbst
	 * der Empfaenger sind ;).
   * @return Empfaenger der Zahlung.
   * @throws RemoteException
   */
  public Empfaenger getEmpfaenger() throws RemoteException;
	
	/**
	 * Betrag der Buchung. Soll-Buchungen werden durch negative
	 * Werte dargestellt, Haben-Buchungen durch positive Werte.
   * @return Buchungsbetrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
	
	/**
	 * Text im Verwendungszweck.
   * @return Verwendungszweck.
   * @throws RemoteException
   */
  public String Zweck() throws RemoteException;
	
	/**
	 * Fortsetzung des Verwendungszwecks.
	 * Das Feld enthaelt alle restlichen Zeilen.
   * @return weiterer Verwendungszweck.
   * @throws RemoteException
   */
  public String getZweck2() throws RemoteException;
	
	/**
	 * Liefert das Datum der Buchung.
   * @return Datum der Buchung.
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;
	
	/**
	 * Datum der Wert-Stellung. 
	 * Das ist das Datum, ab dem der gebuchte Betrag
	 * finanzmathematisch Geltung findet.
	 * Oft stimmt der mit dem Datum der Buchung ueberein.
   * @return Valuta.
   * @throws RemoteException
   */
  public Date getValuta() throws RemoteException;

}


/**********************************************************************
 * $Log: Umsatz.java,v $
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/