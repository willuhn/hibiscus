/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractDBSupportImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/04/05 21:40:56 $
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
import java.io.FileReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;

/**
 * Abstrakte Basisklasse fuer den Datenbank-Support.
 */
public abstract class AbstractDBSupportImpl implements DBSupport
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#execute(java.sql.Connection, java.io.File)
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException
  {
    if (sqlScript == null)
      return;

    if (!sqlScript.canRead() || !sqlScript.exists())
      return;

    Logger.info("executing sql script: " + sqlScript.getAbsolutePath());
    
    FileReader reader = null;

    try
    {
      reader  = new FileReader(sqlScript);
      ScriptExecutor.execute(reader,conn);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("error while executing sql script " + sqlScript,e);
    }
    finally
    {
      try
      {
        if (reader != null)
          reader.close();
      }
      catch (Exception e3)
      {
        Logger.error("error while closing file " + sqlScript,e3);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#install()
   */
  public void install() throws RemoteException
  {
    // Leere Dummy-Implementierung
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#getTransactionIsolationLevel()
   */
  public int getTransactionIsolationLevel() throws RemoteException
  {
    return -1;
  }
  
  private long lastCheck = 0;

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
   */
  public void checkConnection(Connection conn) throws RemoteException
  {
    long newCheck = System.currentTimeMillis();
    if ((newCheck - lastCheck) < (10 * 1000L))
      return; // Wir checken hoechstens aller 10 Sekunden
    
    Statement s  = null;
    ResultSet rs = null;
    try
    {
      s = conn.createStatement();
      rs = s.executeQuery("select 1");
      lastCheck = newCheck;
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


}


/*********************************************************************
 * $Log: AbstractDBSupportImpl.java,v $
 * Revision 1.5  2009/04/05 21:40:56  willuhn
 * @C checkConnection() nur noch alle hoechstens 10 Sekunden ausfuehren
 *
 * Revision 1.4  2008/12/30 15:21:40  willuhn
 * @N Umstellung auf neue Versionierung
 *
 * Revision 1.3  2007/07/28 15:51:26  willuhn
 * @B Bug 447
 *
 * Revision 1.2  2007/07/18 09:45:18  willuhn
 * @B Neue Version 1.8 in DB-Checks nachgezogen
 *
 * Revision 1.1  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 **********************************************************************/