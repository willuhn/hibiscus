/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Umsatz.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/03/05 00:04:10 $
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
	 * Liefert das Konto, auf welches sich diese Umsaetze beziehen.
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
  public String getZweck() throws RemoteException;
	
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


	/**
	 * Speichert das Konto, auf welches sich der Umsatz bezieht.
   * @param k das Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto k) throws RemoteException;

	/**
	 * Speichert den Empfaenger des Umsatzes.
   * @param e Empfaenger.
   * @throws RemoteException
   */
  public void setEmpfaenger(Empfaenger e) throws RemoteException;
	
	/**
	 * Betrag der Buchung. Soll-Buchungen werden durch negative Werte dargestellt.
   * @param d Betrag der Buchung.
   * @throws RemoteException
   */
  public void setBetrag(double d) throws RemoteException;
	
	/**
	 * Verwendungszweck.
   * @param zweck
   * @throws RemoteException
   */
  public void setZweck(String zweck) throws RemoteException;
	
	/**
	 * weiterer Verwendungszweck. Darf <code>null</code> sein.
   * @param zweck2
   * @throws RemoteException
   */
  public void setZweck2(String zweck2) throws RemoteException;
	
	/**
	 * Datum der Buchung.
   * @param d
   * @throws RemoteException
   */
  public void setDatum(Date d) throws RemoteException;
	
  /**
   * Datum der Wertstellung.
   * @param d
   * @throws RemoteException
   */
  public void setValuta(Date d) throws RemoteException;



}


/**********************************************************************
 * $Log: Umsatz.java,v $
 * Revision 1.2  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.1  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 **********************************************************************/