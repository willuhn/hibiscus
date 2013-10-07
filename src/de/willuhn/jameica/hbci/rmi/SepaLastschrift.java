/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;




/**
 * Bildet eine SEPA-Lastschrift ab.
 */
public interface SepaLastschrift extends BaseUeberweisung, Duplicatable
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
