/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/LoginHelper.java,v $
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Login;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

import sun.misc.BASE64Encoder;

/**
 * Hilfsklasse fuer's Login.
 * @author willuhn
 */
public class LoginHelper
{

  /**
   * Erzeugt eine base64-codierte MD5-Checksumme des uebergebenen Strings.
   * @param s String.
   * @return Checksumme.
   * @throws NoSuchAlgorithmException
   */
  protected final static String md5(String s) throws NoSuchAlgorithmException
  {
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] b = md.digest(s.getBytes());

    BASE64Encoder encoder = new BASE64Encoder();
    return encoder.encode(b);
  }

  /**
   * Versucht mit den uebergebenen Daten ein Login.
   * Hinweis: Die Funktion liefert niemals ein <code>null</code> wenn das
   * Login nicht gefunden wurde sondern wirft im Fehlerfall generell eine
   * RemoteException.
   * @param username Username.
   * @param password Passwort.
   * @return das erzeugte Login-Objekt.
   * @throws RemoteException wird nur dann nicht geworfen, wenn das Login erfolgreich war.
   */
  public final static Login login(String username, String password) throws RemoteException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    Login l = null;
    try
    {
      DBIterator list = Settings.getDBService().createList(Login.class);
      list.addFilter("username is not null");
      list.addFilter("username != ''");
      list.addFilter("username = '" + username + "'");
      list.addFilter("password is not null");
      list.addFilter("password != ''");
      list.addFilter("password = '" + md5(password) + "'");
      if (list.hasNext())
        l = (Login) list.next();
    }
    catch (Throwable t)
    {
      throw new RemoteException(i18n.tr("Fehler beim Login. Bitte prüfen Sie das Systemprotokoll"),t);
    }
    if (l == null || l.isNewObject())
      throw new RemoteException(i18n.tr("Login fehlgeschlagen. Benutzername oder Passwort falsch"));
    return l;
  }
}


/*********************************************************************
 * $Log: LoginHelper.java,v $
 * Revision 1.1  2004/11/15 18:09:18  willuhn
 * @N Login fuer die gesamte Anwendung
 *
 **********************************************************************/