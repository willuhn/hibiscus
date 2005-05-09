/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Nachricht.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/09 17:26:56 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer eine System-Nachricht der Bank.
 */
public interface Nachricht extends DBObject
{
  /**
   * Liefert die BLZ dieser Nachricht.
   * @return BLZ
   * @throws RemoteException
   */
  public String getBLZ() throws RemoteException;

  /**
   * Liefert das Datum der Nachricht.
   * @return Datum
   * @throws RemoteException
   */
  public Date getDatum() throws RemoteException;

  /**
   * Prueft, ob die Nachricht bereits gelesen wurde.
   * @return true, wenn sie gelesen wurde.
   * @throws RemoteException
   */
  public boolean isGelesen() throws RemoteException;

  /**
   * Liefert den Nachrichtentext.
   * @return Nachrichtentext.
   * @throws RemoteException
   */
  public String getNachricht() throws RemoteException;

  /**
   * Speichert die BLZ.
   * @param blz
   * @throws RemoteException
   */
  public void setBLZ(String blz) throws RemoteException;

  /**
   * Speichert das Datum.
   * @param datum
   * @throws RemoteException
   */
  public void setDatum(Date datum) throws RemoteException;

  /**
   * Markiert die Nachricht als gelesen/ungelesen.
   * @param b
   * @throws RemoteException
   */
  public void setGelesen(boolean b) throws RemoteException;

  /**
   * Speichert den Nachrichtentext.
   * @param nachricht
   * @throws RemoteException
   */
  public void setNachricht(String nachricht) throws RemoteException;

}


/**********************************************************************
 * $Log: Nachricht.java,v $
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/