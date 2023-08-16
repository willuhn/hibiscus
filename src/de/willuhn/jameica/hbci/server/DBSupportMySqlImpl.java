/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.MessageFormat;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Datenbank-Supports fuer MySQL.
 */
public class DBSupportMySqlImpl extends AbstractDBSupportImpl
{
  private final static String DRIVER = "org.mariadb.jdbc.Driver";
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.mysql.jdbcdriver",DRIVER);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    return this.getEncrypted("database.driver.mysql.password");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.mysql.jdbcurl","jdbc:mariadb://localhost:3306/hibiscus?useUnicode=Yes&characterEncoding=ISO8859_1&serverTimezone=Europe/Paris");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.mysql.username","hibiscus");
  }

  /**
   * Ueberschrieben, weil SQL-Scripts bei MySQL nicht automatisch durchgefuehrt werden.
   * Andernfalls wuerde jeder Hibiscus-Client beim ersten Start versuchen, diese anzulegen.
   * Das soll der Admin sicherheitshalber manuell durchfuehren. Wir hinterlassen stattdessen
   * nur einen Hinweistext mit den auszufuehrenden SQL-Scripts.
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return; // Ignore

    File f = new File(sqlScript.getParent(),getScriptPrefix() + sqlScript.getName());
    if (f.exists())
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      
      String text = i18n.tr("Bei der Verwendung von MySQL wird die Datenbank " +
          "nicht automatisch angelegt. Bitte führen Sie das folgende SQL-Script " +
          "manuell aus, falls Sie dies nicht bereits getan haben:\n{0}",f.getAbsolutePath());
      
      BootMessage msg = new BootMessage(text);
      msg.setTitle(i18n.tr("Hinweis zur Verwendung von MySQL"));
      Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(msg);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getScriptPrefix()
   */
  public String getScriptPrefix() throws RemoteException
  {
    return "mysql-";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return MessageFormat.format("(UNIX_TIMESTAMP({0})*1000)", content);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#getTransactionIsolationLevel()
   */
  public int getTransactionIsolationLevel() throws RemoteException
  {
    // damit sehen wir Datenbank-Updates durch andere
    // ohne vorher ein COMMIT machen zu muessen
    // Insbesondere bei MySQL sinnvoll.
    return Connection.TRANSACTION_READ_COMMITTED;
  }

}
