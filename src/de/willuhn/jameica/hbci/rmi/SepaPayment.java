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
 * Basis-Interface fuer einen SEPA-Auftrag.
 */
public interface SepaPayment
{
  /**
   * Liefert die optionale PmtInf-ID fuer SEPA.
   * @return die optionale PmtInf-ID fuer SEPA.
   * @throws RemoteException
   */
  public String getPmtInfId() throws RemoteException;

  /**
   * Speichert die optionale PmtInf-ID fuer SEPA.
   * @param id die optionale PmtInf-ID fuer SEPA.
   * @throws RemoteException
   */
  public void setPmtInfId(String id) throws RemoteException;

}
