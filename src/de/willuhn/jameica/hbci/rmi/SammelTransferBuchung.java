/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelTransferBuchung.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/20 14:49:05 $
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
 * Interface fuer eine einzelne Buchung eines Sammel-Transfers.
 */
public interface SammelTransferBuchung extends DBObject
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

}


/**********************************************************************
 * $Log: SammelTransferBuchung.java,v $
 * Revision 1.2  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/