/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelLastBuchung.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/27 17:11:49 $
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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer eine einzelne Buchung einer Sammellastschrift.
 */
public interface SammelLastBuchung extends DBObject, Checksum
{
	/**
	 * Liefert die zugeordnete Sammellastschrift.
   * @return Sammellastschrift.
   * @throws RemoteException
   */
  public SammelLastschrift getSammelLastschrift() throws RemoteException;

	/**
	 * Legt die zugehoerige Sammellastschrift fest.
   * @param s Sammellastschrift.
   * @throws RemoteException
   */
  public void setSammelLastschrift(SammelLastschrift s) throws RemoteException;

	/**
	 * Liefert die Kontonummer des Gegenkontos.
   * @return Kontonummer.
   * @throws RemoteException
   */
  public String getGegenkontoNummer() throws RemoteException;
	
	/**
	 * Liefert die BLZ des Gegenkontos.
   * @return BLZ.
   * @throws RemoteException
   */
  public String getGegenkontoBLZ() throws RemoteException;

	/**
	 * Liefert den Namen des Kontoinhabers des Gegenkontos.
   * @return Name.
   * @throws RemoteException
   */
  public String getGegenkontoName() throws RemoteException;

	/**
	 * Speichert die Kontonummer des Gegenkontos.
   * @param kontonummer
   * @throws RemoteException
   */
  public void setGegenkontoNummer(String kontonummer) throws RemoteException;
	
	/**
	 * Speichert die BLZ des Gegenkontos.
   * @param blz
   * @throws RemoteException
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException;

	/**
	 * Speichert den Namen des Kontoinhabers des Gegenkontos.
   * @param name
   * @throws RemoteException
   */
  public void setGegenkontoName(String name) throws RemoteException;

	/**
	 * Speichert alle drei Eigenschaften des Gegenkontos mit denen der Adresse. 
   * @param gegenkonto Adresse.
   * @throws RemoteException
   */
  public void setGegenkonto(Adresse gegenkonto) throws RemoteException;

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
	 * Dupliziert die Buchung.
	 * @return neue Buchung mit den gleichen Eigenschaften.
	 * @throws RemoteException
	 */
	public SammelLastBuchung duplicate() throws RemoteException;

}


/**********************************************************************
 * $Log: SammelLastBuchung.java,v $
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 **********************************************************************/