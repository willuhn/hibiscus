/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.Service;

/**
 * Interface fuer die automatische Ausführung der Synchronisierung.
 */
public interface SynchronizeSchedulerService extends Service
{
  /**
   * Liefert Datum und Uhrzeit der naechsten Ausfuehrung.
   * @return Datum und Uhrzeit der naechsten Ausfuehrung.
   * @throws RemoteException
   */
  public Date getNextExecution() throws RemoteException;
  
  /**
   * Liefert den Status der letzten Synchronisierung.
   * @return der Status der letzten Synchronisierung.
   * @throws RemoteException
   */
  public int getStatus() throws RemoteException;
}
