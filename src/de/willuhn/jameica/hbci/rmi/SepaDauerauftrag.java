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
 * Bildet einen SEPA-Dauerauftrag in Hibiscus ab.
 */
public interface SepaDauerauftrag extends HibiscusTransfer, Checksum, BaseDauerauftrag, SepaPayment, SepaBooking
{
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
