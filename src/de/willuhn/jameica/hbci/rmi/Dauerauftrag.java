/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Dauerauftrag.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/07/11 16:14:29 $
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

/**
 * Bildet einen Dauerauftrag in Hibiscus ab.
 */
public interface Dauerauftrag extends Transfer
{

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
	 * Liefert den Zahlungsturnus fuer diesen Dauerauftrag.
   * @return Zahlungsturnus des Dauerauftrags.
   * @throws RemoteException
   */
  public Turnus getTurnus() throws RemoteException;

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

}


/**********************************************************************
 * $Log: Dauerauftrag.java,v $
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/