/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/LoginImpl.java,v $
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

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Login;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Logins fuer Hibiscus.
 * @author willuhn
 */
public class LoginImpl extends AbstractDBObject implements Login
{

  private I18N i18n;

  /**
   * @throws RemoteException
   */
  public LoginImpl() throws RemoteException
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "login";
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "username";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
   */
  protected void deleteCheck() throws ApplicationException
  {
    // kann getrost geloescht werden.
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getUsername() == null || getUsername().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Benutzernamen ein"));

      String password = (String) getAttribute("password");
      if (password == null || password.length() == 0)
      {
        throw new ApplicationException(i18n.tr("Bitte geben Sie ein Passwort ein"));
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Login-Daten"),e);
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    this.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String field) throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Login#getUsername()
   */
  public String getUsername() throws RemoteException
  {
    return (String) getAttribute("username");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Login#setPassword(java.lang.String, java.lang.String)
   */
  public void setPassword(String oldPassword, String newPassword)
    throws RemoteException
  {
    if (newPassword == null || newPassword.length() == 0)
      throw new RemoteException(i18n.tr("Bitte geben Sie ein neues Passwort ein"));

    String old = (String) getAttribute("password");
    try
    {
      if (old != null && old.length() > 0)
      {
        // wir pruefen, ob das alte Passwort stimmt
          if (!old.equals(LoginHelper.md5(oldPassword)))
            throw new RemoteException(i18n.tr("Die Eingabe des bisherigen Passworts ist falsch"));
      }
      setAttribute("password",LoginHelper.md5(newPassword));
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Erstellen der MD5-Summe"),e);
    }
  }
}


/*********************************************************************
 * $Log: LoginImpl.java,v $
 * Revision 1.1  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
 **********************************************************************/