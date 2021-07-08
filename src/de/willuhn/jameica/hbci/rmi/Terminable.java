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
 * Interface fuer clientseitig terminierte Transfers.
 * Das sind alle Geld-Transfers, die im Hibiscus-eigenen Terminkalender verwaltet werden.
 */
public interface Terminable
{

	/**
	 * Liefert den Termin der Ueberweisung.
   * @return Termin der Ueberweisung.
   * @throws RemoteException
   */
  public Date getTermin() throws RemoteException;

	/**
	 * Speichert den Termin, an dem die Ueberweisung ausgefuehrt werden soll.
   * @param termin Termin der Ueberweisung.
   * @throws RemoteException
   */
  public void setTermin(Date termin) throws RemoteException;

  /**
   * Prueft, ob die Ueberweisung ueberfaellig ist.
   * @return true, wenn sie ueberfaellig ist.
   * @throws RemoteException
   */
  public boolean ueberfaellig() throws RemoteException;

  /**
   * Prueft, ob das Objekt ausgefuehrt wurde.
   * @return true, wenn das Objekt bereits ausgefuehrt wurde.
   * @throws RemoteException
   */
  public boolean ausgefuehrt() throws RemoteException;

  /**
   * Liefert das Datum, zu dem der Auftrag ausgefuehrt wurde.
   * @return das Datum zu dem der Auftrag ausgefuehrt wurde.
   * @throws RemoteException
   */
  public Date getAusfuehrungsdatum() throws RemoteException;

  /**
   * Markiert das Objekt als ausgefuehrt/nicht ausgefuehrt und speichert die Aenderung
   * unmittelbar.
   * @param b ausgefuehrt-Status.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void setAusgefuehrt(boolean b) throws RemoteException, ApplicationException;

}
