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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer Sammellastschriften und -ueberweisungen.
 */
public interface SammelTransfer extends HibiscusDBObject, Terminable
{
	/**
	 * Liefert eine Liste der Buchungen fuer diesen Transfer.
   * Das sind Objekte des Typs <code>SammelTransferBuchung</code>.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public DBIterator getBuchungen() throws RemoteException;

  /**
   * Liefert die Buchungen des Sammeltransfers als Array.
   * Convenience-Funktion fuer Velocity (fuer den Export). Das versteht leider nur Arrays/List,
   * kann also nicht mit einem DBIterator umgehen.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public SammelTransferBuchung[] getBuchungenAsArray() throws RemoteException;
  
  /**
   * Liefert die Summe der enthaltenen Buchungen.
   * @return Summe der enthaltenen Buchungen.
   * @throws RemoteException
   */
  public double getSumme() throws RemoteException;

  /**
	 * Liefert das Konto, ueber das der Transfer gebucht wird.
   * @return Konto.
   * @throws RemoteException
   */
  public Konto getKonto() throws RemoteException;

	/**
	 * Speichert das Konto, ueber das der Transfer gebucht werden soll.
   * @param konto Konto.
   * @throws RemoteException
   */
  public void setKonto(Konto konto) throws RemoteException;

  /**
   * Liefert eine Bezeichnung des Transfers.
   * @return Bezeichnung.
   * @throws RemoteException
   */
  public String getBezeichnung() throws RemoteException;

  /**
   * Speichert die Bezeichnung.
   * @param bezeichnung
   * @throws RemoteException
   */
  public void setBezeichnung(String bezeichnung) throws RemoteException;
  
  /**
   * Erzeugt eine neue Buchung auf dem Sammeltransfer.
   * @return die neu erzeugte Buchung.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException;
  
  /**
   * Prueft, ob bei der Ausfuehrung des Auftrages Warnungen auftraten.
   * @return true, wenn Warnungen auftraten.
   * @throws RemoteException
   */
  public boolean hasWarnings() throws RemoteException;
  
  /**
   * Legt fest, ob bei der Ausfuehrung Warnungen auftraten.
   * @param b true, wenn Warnungen auftraten.
   * @throws RemoteException
   */
  public void setWarning(boolean b) throws RemoteException;

}
