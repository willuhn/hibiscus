/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Attic/HBCIUpdateProvider.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/07 00:48:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.hbci.server.VersionUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Update-Providers fuer Hibiscus.
 */
public class HBCIUpdateProvider implements UpdateProvider
{
  private HBCI plugin     = null;  
  private Version version = null;
  private Connection conn = null;

  /**
   * ct
   * @param name
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected HBCIUpdateProvider(HBCI plugin, Connection conn, String name) throws ApplicationException, RemoteException
  {
    this.plugin  = plugin;
    this.conn    = conn;
    this.version = VersionUtil.getVersion(name);
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getConnection()
   */
  public synchronized Connection getConnection() throws ApplicationException
  {
    return this.conn;
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getCurrentVersion()
   */
  public int getCurrentVersion() throws ApplicationException
  {
    try
    {
      return this.version.getVersion();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to read current version number");
      throw new ApplicationException(plugin.getResources().getI18N().tr("Fehler beim Ermitteln der aktuellen Versionsnummer"));
    }
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getProgressMonitor()
   */
  public ProgressMonitor getProgressMonitor()
  {
    // Liefert den Splashscreen oder im Servermode einen
    // Pseudo-Monitor.
    return Application.getController().getApplicationCallback().getStartupMonitor();
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getUpdatePath()
   */
  public File getUpdatePath() throws ApplicationException
  {
    // Ist das Unterverzeichnis "plugins" im Plugin
    return new File(plugin.getResources().getPath(),"updates");
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#setNewVersion(int)
   */
  public void setNewVersion(int newVersion) throws ApplicationException
  {
    int current = getCurrentVersion();
    try
    {
      this.version.setVersion(newVersion);
      this.version.store();
    }
    catch (Exception e)
    {
      // Im Fehlerfall Versionsnummer zuruecksetzen
      try
      {
        this.version.setVersion(current);
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback version",e2);
        // Werfen wir nicht, weil es sonst die eigentliche Exception verdecken wuerde
      }

      if (e instanceof ApplicationException)
        throw (ApplicationException) e;
      
      Logger.error("unable to read current version number",e);
      throw new ApplicationException(plugin.getResources().getI18N().tr("Fehler beim Ermitteln der aktuellen Versionsnummer"));
    }
  }

}


/*********************************************************************
 * $Log: HBCIUpdateProvider.java,v $
 * Revision 1.2  2007/12/07 00:48:05  willuhn
 * @N weiterer Code fuer den neuen Update-Mechanismus
 *
 * Revision 1.1  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/