/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Dauerauftrag.java,v $
 * $Revision: 1.10 $
 * $Date: 2005/03/04 00:50:16 $
 * $Author: web0 $
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

import de.willuhn.util.ApplicationException;

/**
 * Bildet einen Dauerauftrag in Hibiscus ab.
 */
public interface Dauerauftrag extends Transfer, Checksum
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


/**********************************************************************
 * $Log: Dauerauftrag.java,v $
 * Revision 1.10  2005/03/04 00:50:16  web0
 * @N Eingrauen abgelaufener Dauerauftraege
 * @N automatisches Loeschen von Dauerauftraegen, die lokal zwar
 * noch als aktiv markiert sind, bei der Bank jedoch nicht mehr existieren
 *
 * Revision 1.9  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.8  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.5  2004/10/24 17:19:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.2  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/