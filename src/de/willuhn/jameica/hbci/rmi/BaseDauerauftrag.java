/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface fuer Dauerauftraege in Hibiscus.
 */
public interface BaseDauerauftrag extends HibiscusTransfer, Checksum
{
  /**
   * Platzhalter-Order-ID fuer Banken, die bei der Dauerauftrags-Einreichung
   * keine Order-ID liefern. Dann koennen wir trotzdem wenigstens erkennen,
   * ob der Auftrag eingereicht wurde.
   */
  public final static String ORDERID_PLACEHOLDER = "9999999999";

	/**
	 * Liefert das Datum der ersten Zahlung.
   * @return erste Zahlung.
   * @throws RemoteException
   */
  public Date getErsteZahlung() throws RemoteException;
	
	/**
	 * Liefert das Datum der letzten Zahlung oder <code>null</code>, wenn kein Zahlungsende definiert ist.
   * @return Datum der letzten Zahlung oder <code>null</code>.
   * @throws RemoteException
   */
  public Date getLetzteZahlung() throws RemoteException;

  /**
   * BUGZILLA 204
   * Liefert das voraussichtliche Datum der naechsten Zahlung.
   * Liegt das Datum der ersten Zahlung in der Zukunft, wird dieses
   * zurueckgeliefert. Liegt das Datum der letzten Zahlung in der Vergangenheit,
   * gilt der Dauerauftrag als abgelaufen und es wird <code>null</code>
   * zurueckgeliefert. Andernfalls wird anhand des Zahlungsturnus das
   * naechste Zahl-Datum ermittelt.
   * @return Datum der naechsten Zahlung.
   * @throws RemoteException
   */
  public Date getNaechsteZahlung() throws RemoteException;

	/**
	 * Liefert den Zahlungsturnus fuer diesen Dauerauftrag.
   * @return Zahlungsturnus des Dauerauftrags.
   * @throws RemoteException
   */
  public Turnus getTurnus() throws RemoteException;

	/**
	 * Liefert die eindeutige ID von der Bank.
	 * Damit kann der Dauerauftrag bei Aenderungen wiedererkannt werden.
   * @return Order-ID.
   * @throws RemoteException
   */
  public String getOrderID() throws RemoteException;

  /**
	 * Speichert die Order-ID des Dauerauftrages.
   * @param id die Order-ID.
   * @throws RemoteException
   */
  public void setOrderID(String id) throws RemoteException;

	/**
	 * Legt das Datum fuer die erste Zahlung fest.
   * @param datum Datum fuer die erste Zahlung.
   * @throws RemoteException
   */
  public void setErsteZahlung(Date datum) throws RemoteException;

	/**
	 * Legt das Datum fuer die letzte Zahlung fest.
   * @param datum Datum fuer die letzte Zahlung. Kann <code>null</code> sein, wenn kein End-Datum definiert ist.
   * @throws RemoteException
   */
  public void setLetzteZahlung(Date datum) throws RemoteException;

	/**
	 * Legt den Zahlungsturnus fest.
   * @param turnus Zahlungsturnus des Dauerauftrags.
   * @throws RemoteException
   */
  public void setTurnus(Turnus turnus) throws RemoteException;

	/**
	 * Liefert <code>true</code> wenn der Dauerauftrag bei der Bank aktiv ist.
	 * Ob dieser nun von der Bank abgerufen oder lokal erstellt und dann
	 * eingereicht wurde, spielt keine Rolle. Entscheidend ist lediglich, dass
	 * er bei der Bank vorliegt und aktiv ist. 
	 * @return true, wenn der Dauerauftrag bei der Bank aktiv ist.
	 * @throws RemoteException
	 */
	public boolean isActive() throws RemoteException;

  /**
   * Loescht den Dauerauftrag lediglich im lokalen Datenbestand, nicht jedoch bei der Bank.
   * Um den Dauerauftrag online zu loeschen, muss <code>HBCIDauerauftragDeleteJob</code>
   * verwendet werden.
   * @see de.willuhn.datasource.rmi.Changeable#delete()
   */
  public void delete() throws RemoteException, ApplicationException;

}
