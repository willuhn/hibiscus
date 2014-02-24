/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer SEPA-Sammellastschriften und -ueberweisungen.
 * @param <T> der konkrete Typ der enthaltenen Buchungen.
 */
public interface SepaSammelTransfer<T extends SepaSammelTransferBuchung> extends HibiscusDBObject, Terminable, Duplicatable
{
	/**
	 * Liefert eine Liste der Buchungen fuer diesen Transfer.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public List<T> getBuchungen() throws RemoteException;

  /**
   * Liefert die Summe der enthaltenen Buchungen.
   * @return Summe der enthaltenen Buchungen.
   * @throws RemoteException
   */
  public BigDecimal getSumme() throws RemoteException;

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
  public T createBuchung() throws RemoteException, ApplicationException;
}
