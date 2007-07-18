/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractDBSupportImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/07/18 09:45:18 $
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

import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.util.ApplicationException;

/**
 * Abstrakte Basisklasse fuer den Datenbank-Support.
 */
public abstract class AbstractDBSupportImpl implements DBSupport
{

  /**
   * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConsistency(java.sql.Connection)
   */
  public void checkConsistency(Connection conn) throws RemoteException, ApplicationException
  {
    // Leere Dummy-Implementierung
  }

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

}


/*********************************************************************
 * $Log: AbstractDBSupportImpl.java,v $
 * Revision 1.2  2007/07/18 09:45:18  willuhn
 * @B Neue Version 1.8 in DB-Checks nachgezogen
 *
 * Revision 1.1  2007/04/19 18:12:21  willuhn
 * @N MySQL-Support (GUI zum Konfigurieren fehlt noch)
 *
 **********************************************************************/