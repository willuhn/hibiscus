/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Ueberweisung.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/17 01:01:38 $
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
import de.willuhn.util.ApplicationException;

/**
 * Bildet eine Ueberweisung ab.
 */
public interface Ueberweisung extends DBObject {

	public final static double BETRAG_MAX = 10000d; 
	
	/**
	 * Liefert das Konto, ueber das bezahlt wurde.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;
	
	/**
	 * Liefert den Empfaenger.
   * @return
   * @throws RemoteException
   */
  public Empfaenger getEmpfaenger() throws RemoteException;
	
	/**
	 * Liefert den Betrag.
   * @return Betrag.
   * @throws RemoteException
   */
  public double getBetrag() throws RemoteException;
	
	/**
	 * Liefert die Zeile 1 des Verwendungszwecks.
   * @return Zeile 1 des Verwendungszwecks.
   * @throws RemoteException
   */
  public String getZweck() throws RemoteException;
	
	/**
	 * Liefert die Zeile 2 des Verwendungszwecks.
	 * @return Zeile 2 des Verwendungszwecks.
	 * @throws RemoteException
	 */
	public String getZweck2() throws RemoteException;
	
	/**
	 * Liefert den Termin der Ueberweisung.
   * @return Termin der Ueberweisung.
   * @throws RemoteException
   */
  public Date getTermin() throws RemoteException;
	
	/**
	 * Prueft, ob die Ueberweisung ausgefuehrt wurde.
   * @return true, wenn die Ueberweisung bereits ausgefuehrt wurde.
   * @throws RemoteException
   */
  public boolean ausgefuehrt() throws RemoteException;
	
	/**
	 * Speichert das Konto, das zur Bezahlung verwendet werden soll.
   * @param konto Konto, das verwendet werden soll.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;
	
	/**
	 * Speichert den Empfaenger.
   * @param empfaenger Empfaenger des Geldes.
   * @throws RemoteException
   */
  public void setEmpfaenger(Empfaenger empfaenger) throws RemoteException;
	
	/**
	 * Speichert den zu ueberweisenden Betrag.
   * @param betrag Betrag.
   * @throws RemoteException
   */
  public void setBetrag(double betrag) throws RemoteException;
	
	/**
	 * Speichert den Zweck der Ueberweisung.
   * @param zweck Zweck der Ueberweisung.
   * @throws RemoteException
   */
  public void setZweck(String zweck) throws RemoteException;
	
	/**
	 * Speichert Zeile 2 des Verwendungszwecks.
   * @param zweck2 Zeile 2 des Verwendungszwecks.
   * @throws RemoteException
   */
  public void setZweck2(String zweck2) throws RemoteException;

	/**
	 * Speichert den Termin, an dem die Ueberweisung ausgefuehrt werden soll.
   * @param termin Termin der Ueberweisung.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

	/**
	 * Fuehrt die Ueberweisung aus zum definierten Termin aus.
	 * Ist dieser Termin nicht definiert, wird sie sofort ausgefuehrt.
   * @throws ApplicationException
   */
  public void execute() throws RemoteException, ApplicationException;
	
}


/**********************************************************************
 * $Log: Ueberweisung.java,v $
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/