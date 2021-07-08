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
 * Basis-Interface fuer eine SEPA-Buchung.
 */
public interface SepaBooking extends Transfer
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
   * Liefert den optionalen Purpose-Code.
   * @return der optionale Purpose-Code.
   * @throws RemoteException
   */
  public String getPurposeCode() throws RemoteException;

  /**
   * Speichert den optionalen Purpose-Code.
   * @param code der optionale Purpose-Code.
   * @throws RemoteException
   */
  public void setPurposeCode(String code) throws RemoteException;

}
