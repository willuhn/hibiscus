/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Bildet einen SEPA-Dauerauftrag in Hibiscus ab.
 */
public interface SepaDauerauftrag extends HibiscusTransfer, Checksum, BaseDauerauftrag
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
   * Liefert true, wenn die Bank mitgeteilt hat, dass der Auftrag aenderbar ist.
   * @return true, wenn die Bank mitgeteilt hat, dass der Auftrag aenderbar ist.
   * @throws RemoteException
   */
  public boolean canChange() throws RemoteException;
  
  /**
   * Liefert true, wenn die Bank mitgeteilt hat, dass der Auftrag geloescht werden darf.
   * @return true, wenn die Bank mitgeteilt hat, dass der Auftrag geloescht werden darf.
   * @throws RemoteException
   */
  public boolean canDelete() throws RemoteException;
}
