/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

/**
 * Bildet einen Dauerauftrag in Hibiscus ab.
 */
public interface Dauerauftrag extends HibiscusTransfer, Checksum, BaseDauerauftrag
{
  /**
   * Liefert den Textschluessel des Auftrags.
   * @return Textschluessel.
   * @throws RemoteException
   */
  public String getTextSchluessel() throws RemoteException;

  /**
   * Speichert den Textschluessel.
   * @param schluessel
   * @throws RemoteException
   */
  public void setTextSchluessel(String schluessel) throws RemoteException;
}
