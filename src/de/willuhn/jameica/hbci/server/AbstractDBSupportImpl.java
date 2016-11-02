/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractDBSupportImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/07/25 10:05:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

    // Wir schreiben unseren Prefix davor.
    sqlScript = new File(sqlScript.getParent(),getScriptPrefix() + sqlScript.getName());
    if (!sqlScript.exists())
    {
      Logger.debug("file " + sqlScript + " does not exist, skipping");
      return;
    }
    
    if (!sqlScript.canRead() || !sqlScript.exists())
      return;

    Logger.info("executing sql script: " + sqlScript.getAbsolutePath());
    
    Reader reader = null;

    try
    {
      reader  = new InputStreamReader(new BufferedInputStream(new FileInputStream(sqlScript)),"iso-8859-1");
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
      // nicht (das muellt uns nur das Log voll) Also fangen wir sie und werfen eine neue
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
 * Revision 1.7  2011/07/25 10:05:32  willuhn
 * @N explizites Encoding
 *
 * Revision 1.6  2010-11-02 12:02:19  willuhn
 * @R Support fuer McKoi entfernt. User, die noch dieses alte DB-Format nutzen, sollen erst auf Jameica 1.6/Hibiscus 1.8 (oder maximal Jameica 1.9/Hibiscus 1.11) wechseln, dort die Migration auf H2 durchfuehren und dann erst auf Hibiscus 1.12 updaten
 *
 * Revision 1.5  2009/04/05 21:40:56  willuhn
 * @C checkConnection() nur noch alle hoechstens 10 Sekunden ausfuehren
 **********************************************************************/