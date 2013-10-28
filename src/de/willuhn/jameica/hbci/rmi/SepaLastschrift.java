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
  
  /**
   * Liefert den Sequenz-Typ der Lastschrft.
   * @return der Sequenz-Typ der Lastschrift.
   * @throws RemoteException
   */
  public SepaLastSequenceType getSequenceType() throws RemoteException;
  
  /**
   * Speichert den Sequenz-Typ der Lastschrift.
   * @param type der Sequenz-Typ der Lastschrift.
   * @throws RemoteException
   */
  public void setSequenceType(SepaLastSequenceType type) throws RemoteException;
  
  /**
   * Liefert das Ziel-Ausfuehrungsdatum bei der Bank.
   * @return das Ziel-Ausfuehrungsdatum bei der Bank.
   * @throws RemoteException
   */
  public Date getTargetDate() throws RemoteException;
  
  /**
   * Speichert das Ziel-Ausfuehrungsdatum bei der Bank.
   * @param date das Ziel-Ausfuehrungsdatum bei der Bank.
   * @throws RemoteException
   */
  public void setTargetDate(Date date) throws RemoteException;
  
  /**
   * Liefert den Typ der Lastschrft.
   * @return der Typ der Lastschrift.
   * @throws RemoteException
   */
  public SepaLastType getType() throws RemoteException;
  
  /**
   * Speichert den Typ der Lastschrift.
   * @param type der Typ der Lastschrift.
   * @throws RemoteException
   */
  public void setType(SepaLastType type) throws RemoteException;
  
  /**
   * Liefert die von der Bank nach der Uebertragung zurueckgemeldete Order-ID.
   * @return die Order-ID. NULL, wenn der Auftrag noch nicht an die Bank gesendet wurde.
   * @throws RemoteException
   */
  public String getOrderId() throws RemoteException;
  
  /**
   * Speichert die von der Bank zurueckgemeldete Order-ID.
   * @param orderId die von der Bank zurueckgemeldete Order-ID.
   * @throws RemoteException
   */
  public void setOrderId(String orderId) throws RemoteException;
  
}
