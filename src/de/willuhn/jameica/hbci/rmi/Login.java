/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Attic/Login.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/11/15 18:09:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer das Login-Objekt.
 * @author willuhn
 */
public interface Login extends DBObject
{
  /**
   * Liefert den Benutzernamen.
   * @return Benutzername.
   * @throws RemoteException
   */
  public String getUsername() throws RemoteException;

  /**
   * Speichert ein neues Passwort fuer den Benutzer.
   * @param oldPassword altes Passwort.
   * Ist noch keines angegeben oder wurde das Login noch nicht gespeichert,
   * kann hier <code>null</code> angegeben werden.
   * @param newPassword das neue Passwort.
   * @throws RemoteException
   */
  public void setPassword(String oldPassword, String newPassword) throws RemoteException;
}


/*********************************************************************
 * $Log: Login.java,v $
 * Revision 1.1  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
 **********************************************************************/