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
}
