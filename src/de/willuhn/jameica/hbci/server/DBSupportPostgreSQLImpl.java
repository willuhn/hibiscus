/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DBSupportPostgreSQLImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/04/01 20:59:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
    return HBCIDBService.SETTINGS.getString("database.driver.postgresql.password",null);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
   */
  public String getJdbcUrl()
  {
    return HBCIDBService.SETTINGS.getString("database.driver .postgresql.jdbcurl","jdbc:postgresql://localhost:5432/hibiscus");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
   */
  public String getJdbcUsername()
  {
    return HBCIDBService.SETTINGS.getString("database.driver.postgresql.username","hibiscus");
  }

  /**
   * Ueberschrieben, weil SQL-Scripts bei MySQL nicht automatisch durchgefuehrt werden.
   * Das soll der Admin sicherheitshalber manuell durchfuehren. Wir hinterlassen stattdessen
   * nur einen Hinweistext mit den auszufuehrenden SQL-Scripts.
   * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return; // Ignore

    String prefix = HBCIDBService.SETTINGS.getString("database.driver.postgresql.scriptprefix","postgresql-");
    File f = new File(sqlScript.getParent(),prefix + sqlScript.getName());
    if (f.exists())
    {
      I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
      
      String text = i18n.tr("Bei der Verwendung von PostgreSQL werden Datenbank-Updates " +
          "nicht automatisch ausgefuehrt. Bitte fuehren Sie das folgende SQL-Script " +
          "manuell aus:\n{0}",f.getAbsolutePath());
      Application.addWelcomeMessage(text);
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
   */
  public String getSQLTimestamp(String content) throws RemoteException
  {
    return MessageFormat.format("({0}::timestamp)", new Object[]{content});
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    Statement s  = null;
    ResultSet rs = null;
    try
    {
      s = conn.createStatement();
      rs = s.executeQuery("select 1");
    }
    catch (SQLException e)
    {
      // das Ding liefert in getMessage() den kompletten Stacktrace mit, den brauchen wir
      // nicht (das muellt uns nur das Log voll) Also fangen wir sie und werden eine neue
      // saubere mit kurzem Fehlertext
      String msg = e.getMessage();
      if (msg != null && msg.indexOf("\n") != -1)
        msg = msg.substring(0,msg.indexOf("\n"));
      throw new RemoteException(msg);
    }
    finally
    {
      try
      {
        if (rs != null) rs.close();
        if (s != null)  s.close();
      }
      catch (Exception e)
      {
        throw new RemoteException("unable to close statement/resultset",e);
      }
    }
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
 * Revision 1.4  2009/04/01 20:59:39  willuhn
 * @N PostgreSQL-Unterstuetzung ist wieder da. Initialer Commit
 *
 * Revision 1.3  2008/12/17 22:49:09  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.2  2007/09/11 09:26:08  willuhn
 * @N SQL-Update-Hinweis nur anzeigen, wenn Datei existiert
 *
 * Revision 1.1  2007/08/20 15:30:28  willuhn
 * @N PostGreSqlSupport von Ralf Burger
 *

 * @N Erster Code fuer Unterstuetzung von PostgreSQL
 *
 **********************************************************************/