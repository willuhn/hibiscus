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

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer einen Datensatz in der Versionstabelle.
 */
public interface Version extends DBObject
{

  /**
   * Liefert den Namen der Version.
   * @return Name der Version.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;

  /**
   * Speichert den Namen der Version.
   * @param name Name der Version.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;

  /**
   * Liefert den aktuellen Stand der Version.
   * @return Stand der Version.
   * @throws RemoteException
   */
  public int getVersion() throws RemoteException;

  /**
   * Legt die neue Versionsnummer fest.
   * @param newVersion die neue Versionsnummer.
   * @throws RemoteException
   */
  public void setVersion(int newVersion) throws RemoteException;

}

/*********************************************************************
 * $Log: Version.java,v $
 * Revision 1.1  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/