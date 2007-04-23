/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Transfer.java,v $
 * $Revision: 1.9 $
 * $Date: 2007/04/23 18:07:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Basis-Interface fuer eine Geld-Bewegung.
 */
public interface Transfer extends Remote
{

	/**
	 * Liefert die Kontonummer des Gegenkontos.
   * @return Kontonummer des Empfaengers.
   * @throws RemoteException
   */
  public String getGegenkontoNummer() throws RemoteException;

	/**
	 * Liefert die BLZ des Gegenkontos.
	 * @return BLZ des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoBLZ() throws RemoteException;
	
	/**
	 * Liefert den Namen des Kontoinhabers des Gegenkontos.
	 * @return Name des Kontoinhabers des Gegenkontos.
	 * @throws RemoteException
	 */
	public String getGegenkontoName() throws RemoteException;

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
}


/**********************************************************************
 * $Log: Transfer.java,v $
 * Revision 1.9  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 **********************************************************************/