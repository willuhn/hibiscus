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
import java.util.Date;

/**
 * Interface fuer eine einzelne Buchung einer SEPA-Sammellastschrift.
 */
public interface SepaSammelLastBuchung extends SepaSammelTransferBuchung<SepaSammelLastschrift>, Duplicatable
{
  /**
   * Liefert die Mandats-ID.
   * @return die Mandats-ID.
   * @throws RemoteException
   */
  public String getMandateId() throws RemoteException;

  /**
   * Speichert die Mandats-ID.
   * @param id die Mandats-ID.
   * @throws RemoteException
   */
  public void setMandateId(String id) throws RemoteException;

  /**
   * Liefert die Glaeubiger-ID.
   * @return die Glaeubiger-ID.
   * @throws RemoteException
   */
  public String getCreditorId() throws RemoteException;

  /**
   * Speichert die Glaeubiger-ID.
   * @param id die Glaeubiger-ID.
   * @throws RemoteException
   */
  public void setCreditorId(String id) throws RemoteException;

  /**
   * Liefert das Datum der Unterschrift des Mandats.
   * @return das Datum der Unterschrift des Mandats.
   * @throws RemoteException
   */
  public Date getSignatureDate() throws RemoteException;

  /**
   * Speichert das Datum der Unterschrift des Mandats.
   * @param date das Datum der Unterschrift des Mandats.
   * @throws RemoteException
   */
  public void setSignatureDate(Date date) throws RemoteException;
}
