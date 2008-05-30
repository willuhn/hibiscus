/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/DBProperty.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/05/30 14:23:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer einen einzelnen datenbank-gestuetzten Parameter.
 */
public interface DBProperty extends DBObject
{
  /**
   * Liefert den Namen des Parameters.
   * @return Name des Parameters.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Speichert den Namen des Parameters.
   * @param name Name des Parameters.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Liefert den Wert des Parameters.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  public String getValue() throws RemoteException;
  
  /**
   * Speichert den Wert des Parameters.
   * @param value Wert des Parameters.
   * @throws RemoteException
   */
  public void setValue(String value) throws RemoteException;

}


/*********************************************************************
 * $Log: DBProperty.java,v $
 * Revision 1.1  2008/05/30 14:23:48  willuhn
 * @N Vollautomatisches und versioniertes Speichern der BPD und UPD in der neuen Property-Tabelle
 *
 **********************************************************************/