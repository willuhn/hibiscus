/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Turnus.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/08/26 21:23:46 $
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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Bildet einen Turnus bei wiederkehrenden Zahlungen ab.
 */
public interface Turnus extends DBObject, Checksum
{

	/**
	 * Konstante fuer Zeiteinheit woechentlich.
	 */
	public final static int ZEITEINHEIT_WOECHENTLICH			= 1;
	
	/**
	 * Konstante fuer Zeiteinheit monatlich.
	 */
	public final static int ZEITEINHEIT_MONATLICH					= 2;


	/**
	 * Liefert eine sprechende Bezeichnung des Intervalls.
   * @return Bezeichnung des Intervalls.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

	/**
	 * Liefert die Anzahl der Intervalle zwischen den Zahlungen.
	 * Beispiele:<br>
	 * <ul>
	 * 	<li>Zahlung wochentlich: Intervall = <code>1</code>, Zeiteinheit = <code>ZEITEINHEIT_WOECHENTLICH</code></li>
	 * 	<li>Zahlung alle 2 Wochen: Intervall = <code>2</code>, Zeiteinheit = <code>ZEITEINHEIT_WOECHENTLICH</code></li>
	 *  <li>Vierteljaehrlich (quartalsweise): Intervall = <code>3</code>, Zeiteinheit = <code>ZEITEINHEIT_MONATLICH</code></li>
	 *  <li>Jaehrlich: Intervall = <code>12</code>, Zeiteinheit = <code>ZEITEINHEIT_MONATLICH</code></li>
	 * </ul>
   * @return Anzahl der Intervalle zwischen den Zahlungen.
   * @throws RemoteException
   */
  public int getIntervall() throws RemoteException;

  /**
   * Speichert die Anzahl der Intervalle zwischen den Zahlungen.
   * @param intervall Anzahl der Intervalle.
   * @throws RemoteException
   */
  public void setIntervall(int intervall) throws RemoteException;

	/**
	 * Liefert eine Konstante fuer die Zeiteinheit.
	 * Ist der Rueckgabewert = <code>ZEITEINHEIT_WOECHENTLICH</code>, dann wird woechentlich
	 * oder zu einem Vielfachen einer Woche gezahlt.<br>
	 * Bei einem Rueckgabewert = <code>ZEITEINHEIT_MONATLICH</code> wird monatlich
	 * oder einem Vielfachen eines Monats gezahlt.<br>
   * @return Zeiteinheit.
   * @throws RemoteException
   */
  public int getZeiteinheit() throws RemoteException;

	/**
	 * Speichert die Zeiteinheit.
   * @param zeiteinheit Kann einen der beiden Werte <code>ZEITEINHEIT_WOECHENTLICH</code>
   * oder <code>ZEITEINHEIT_MONATLICH</code> besitzen.
   * @throws RemoteException
   */
  public void setZeiteinheit(int zeiteinheit) throws RemoteException;
  
  /**
   * Liefert den Tag, an dem die Zahlung innerhalb der Zeiteinheit ausgefuehrt werden soll.
   * @return Tag, an dem die Zahlung erfolgt.
   * Handelt es sich um eine monatliche Zahlung (<code>ZEITEINHEIT_MONATLICH</code>), kann
   * der Wert zwischen 1 und 31 liegen (Tage des Monats).
   * Bei woechentlicher Zahlung (<code>ZEITEINHEIT_WOECHENTLICH</code>), wird ein Wert zwischen
   * 1 und 7 zurueckgeliefert (Wochentag) wobei 1 Montag ist und 7 demzufolge Sonntag.
   * @throws RemoteException
   */
  public int getTag() throws RemoteException;
  
  /**
   * Speichert den Tag, an dem die Zahlung innerhalb der Zeiteinheit ausgefuehrt werden soll.
   * @param tag Tag, an dem die Zahlung erfolgt.
   * @throws RemoteException
   */
  public void setTag(int tag) throws RemoteException;
  
  /**
   * Liefert <code>true</code> wenn es sich bei dem Turnus um Initial-Daten
   * von Hibiscus handelt, die nicht geloescht werden koennen.
   * @return true, wenn der Datensatz nicht geloescht werden kann.
   * @throws RemoteException
   */
  public boolean isInitial() throws RemoteException;
}


/**********************************************************************
 * $Log: Turnus.java,v $
 * Revision 1.6  2009/08/26 21:23:46  willuhn
 * @C "aller x Wochen/Monate" sagt man wohl nur im Saechsischen ;) Habs geaendert auf "alle x Wochen/Monate". Google liefert mit dieser Schreibweise auch erheblich mehr Treffer
 *
 * Revision 1.5  2004/10/25 17:58:57  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.4  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 * Revision 1.3  2004/07/15 23:39:22  willuhn
 * @N TurnusImpl
 *
 * Revision 1.2  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.1  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 **********************************************************************/