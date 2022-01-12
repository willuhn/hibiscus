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
 * Implementierung des Datenbank-Supports fuer PostgreSQL.
 */
public class DBSupportPostgreSQLImpl extends AbstractDBSupportImpl
{
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
   */
  public String getJdbcDriver()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.postgresql.jdbcdriver","org.postgresql.Driver");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
   */
  public String getJdbcPassword()
  {
    return this.getEncrypted("database.driver.postgresql.password");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.postgresql.jdbcurl","jdbc:postgresql://localhost:5432/hibiscus");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.postgresql.username","hibiscus");
  }

  /**
   * Ueberschrieben, weil SQL-Scripts bei PostreSQL nicht automatisch durchgefuehrt werden.
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
      
      String text = i18n.tr("Bei der Verwendung von PostgreSQL werden Datenbank-Updates " +
          "nicht automatisch ausgefuehrt. Bitte fuehren Sie das folgende SQL-Script " +
          "manuell aus:\n{0}",f.getAbsolutePath());

      BootMessage msg = new BootMessage(text);
      msg.setTitle(i18n.tr("Hinweis zur Verwendung von PostgreSQL"));
      Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(msg);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getScriptPrefix()
   */
  public String getScriptPrefix() throws RemoteException
  {
    return "postgresql-";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return MessageFormat.format("({0}::timestamp)", content);
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
    // Bei PostgreSQL eigentlich nicht sinnvoll, da autocommit der default ist.
    return Connection.TRANSACTION_READ_COMMITTED;
  }

}


/*********************************************************************
 * $Log: DBSupportPostgreSQLImpl.java,v $
 * Revision 1.7  2010/11/02 12:02:19  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.6  2009/04/05 21:40:56  willuhn
 * @C checkConnection() nur noch alle hoechstens 10 Sekunden ausfuehren
 *
 * Revision 1.5  2009/04/03 16:48:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/04/01 20:59:39  willuhn
 * @N PostgreSQL-Unterstuetzung ist wieder da. Initialer Commit
 **********************************************************************/