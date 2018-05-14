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

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;

/**
 * Interface fuer eine unterstuetzte Datenbank.
 * Fuer den Suppoert einer neuen Datenbank (z.Bsp. MySQL)
 * in Hibiscus muss dieses Interface implementiert werden.
 */
public interface DBSupport extends Serializable
{
  /**
   * Liefert die JDBC-URL.
   * @return die JDBC-URL.
   */
  public String getJdbcUrl();

  /**
   * Liefert den Klassennamen des JDBC-Treibers.
   * @return der JDBC-Treiber.
   */
  public String getJdbcDriver();

  /**
   * Liefert den Usernamen des Datenbank-Users.
   * @return Username.
   */
  public String getJdbcUsername();

  /**
   * Liefert das Passwort des Datenbank-Users.
   * @return das Passwort.
   */
  public String getJdbcPassword();
  
  /**
   * Prueft die Datenbankverbindung.
   * @param conn die Datenbank-Connection.
   * @throws RemoteException Wenn die Verbindung defekt ist und vom DB-Service neu erzeugt werden muss.
   */
  public void checkConnection(Connection conn) throws RemoteException;

  /**
   * Fuehrt ein SQL-Update-Script auf der Datenbank aus.
   * @param conn die Datenbank-Connection.
   * @param sqlScript das SQL-Script.
   * @throws RemoteException
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException;
  
  /**
   * Liefert einen Dateinamens-Prefix, der SQL-Scripts vorangestellt werden soll.
   * @return Dateinamens-Prefix.
   * @throws RemoteException
   */
  public String getScriptPrefix() throws RemoteException;

  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;
  
  /**
   * Legt fest, ob SQL-Insert-Queries mit oder ohne ID erzeugt werden sollen.
   * @return true, wenn die Insert-Queries mit ID erzeugt werden.
   * @throws RemoteException
   * Siehe auch: de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException;

  /**
   * Liefert das Transaction-Isolation-Level.
   * @return das Transaction-Isolation-Level.
   * @throws RemoteException
   */
  public int getTransactionIsolationLevel() throws RemoteException;

}


/*********************************************************************
 * $Log: DBSupport.java,v $
 * Revision 1.9  2010/11/02 12:02:20  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.8  2008/12/30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.7  2008/02/04 18:48:18  willuhn
 * @D javadoc
 *
 * Revision 1.6  2007/07/28 15:51:26  willuhn
 * @B Bug 447
 *
 * Revision 1.5  2007/07/16 12:51:15  willuhn
 * @D javadoc
 *
 * Revision 1.4  2007/05/07 09:27:25  willuhn
 * @N Automatisches Neuerstellen der JDBC-Connection bei MySQL
 *
 * Revision 1.3  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 * Revision 1.1  2007/04/18 17:03:06  willuhn
 * @N Erster Code fuer Unterstuetzung von MySQL
 *
 **********************************************************************/