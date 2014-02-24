/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;


/**
 * Interface fuer SEPA-Sammellastschriften.
 */
public interface SepaSammelLastschrift extends SepaSammelTransfer<SepaSammelLastBuchung>
{
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
