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

/**
 * Interface fuer eine einzelne Buchung eines Sammel-Transfers.
 */
public interface SammelTransferBuchung extends Transfer, HibiscusDBObject
{
	/**
	 * Liefert den zugeordneten Sammel-Transfer.
   * @return Sammel-Transfer.
   * @throws RemoteException
   */
  public SammelTransfer getSammelTransfer() throws RemoteException;

	/**
	 * Legt den zugehoerigen Sammel-Transfer fest.
   * @param s Sammel-Transfer.
   * @throws RemoteException
   */
  public void setSammelTransfer(SammelTransfer s) throws RemoteException;

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
   * Liefert den Textschluessel der Buchung.
   * @return Textschluessel.
   * @throws RemoteException
   */
  public String getTextSchluessel() throws RemoteException;
  
  /**
   * Speichert den Textschluessel der Buchung.
   * @param schluessel Textschluessel.
   * @throws RemoteException
   */
  public void setTextSchluessel(String schluessel) throws RemoteException;

  /**
   * Speichert eine Liste erweiterter Verwendungszwecke.
   * @param list Liste erweiterter Verwendungszwecke.
   * @throws RemoteException
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException;
  
  /**
   * Liefert eine ggf aufgetretene Warnung bei der Ausfuehrung.
   * @return eine ggf aufgetretene Warnung bei der Ausfuehrung.
   * @throws RemoteException
   */
  public String getWarnung() throws RemoteException;
  
  /**
   * Speichert eine ggf aufgetretene Warnung bei der Ausfuehrung.
   * @param warnung eine ggf aufgetretene Warnung bei der Ausfuehrung.
   * @throws RemoteException
   */
  public void setWarnung(String warnung) throws RemoteException;
}
