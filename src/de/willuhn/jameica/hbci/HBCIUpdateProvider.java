/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Attic/HBCIUpdateProvider.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/12/06 17:57:21 $
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
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Update-Providers fuer Hibiscus.
 */
public class HBCIUpdateProvider implements UpdateProvider
{
  
  private Version version = null;
  private I18N i18n       = null;

  /**
   * ct
   * @param name
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected HBCIUpdateProvider(String name) throws ApplicationException, RemoteException
  {
    this.version = VersionUtil.getVersion(name);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getConnection()
   */
  public Connection getConnection() throws ApplicationException
  {
    // TODO Auto-generated method stub
    return null;
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
      throw new ApplicationException(i18n.tr("Fehler beim Ermitteln der aktuellen Versionsnummer"));
    }
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getProgressMonitor()
   */
  public ProgressMonitor getProgressMonitor()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getUpdatePath()
   */
  public File getUpdatePath() throws ApplicationException
  {
    // TODO Auto-generated method stub
    return null;
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
      throw new ApplicationException(i18n.tr("Fehler beim Ermitteln der aktuellen Versionsnummer"));
    }
  }

}


/*********************************************************************
 * $Log: HBCIUpdateProvider.java,v $
 * Revision 1.1  2007/12/06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/