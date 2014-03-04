/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;




/**
 * Bildet eine Auslands-Ueberweisung ab.
 */
public interface AuslandsUeberweisung extends BaseUeberweisung, Duplicatable
{
  /**
   * Liefert die optionale End2End-ID fuer SEPA.
   * @return die optionale End2End-ID fuer SEPA.
   * @throws RemoteException
   */
  public String getEndtoEndId() throws RemoteException;
  
  /**
   * Speichert die optionale End2End-ID fuer SEPA.
   * @param id die optionale End2End-ID fuer SEPA.
   * @throws RemoteException
   */
  public void setEndtoEndId(String id) throws RemoteException;
  
  /**
   * Prueft, ob es sich um einen bankseitige Termin-Ueberweisung handelt.
   * @return true, wenn es eine bankseitige Termin-Ueberweisung ist.
   * @throws RemoteException
   */
  public boolean isTerminUeberweisung() throws RemoteException;
  
  /**
   * Legt fest, ob es sich um eine bankseitige Termin-Ueberweisung handelt.
   * @param termin true, wenn es eine bankseitige Terminueberweisung sein soll.
   * @throws RemoteException
   */
  public void setTerminUeberweisung(boolean termin) throws RemoteException;
}
