/**********************************************************************
 *
 * Copyright (c) 2025 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Übernimmt die Migration auf die neue H2-Datenbank-Version.
 */
@Lifecycle(Type.CONTEXT)
public class H2MigrationTask implements BackgroundTask
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
   */
  @Override
  public void run(ProgressMonitor monitor) throws ApplicationException
  {
    try
    {
      final String file = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath() + "/h2db/h2-migration.sql";
      final HBCIDBService db = Settings.getDBService();
      db.execute("SCRIPT SIMPLE TO ?",new Object[]{file},new ResultSetExtractor() {
        
        @Override
        public Object extract(ResultSet rs) throws RemoteException, SQLException
        {
          return null;
        }
      });
    }
    catch (Exception e)
    {
      Logger.error("error while performing h2 migration",e);
      if (e instanceof ApplicationException)
        throw (ApplicationException) e;
      
      throw new ApplicationException(i18n.tr("H2-Migration fehlgeschlagen"),e);
    }
  }

  /**
   * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
   */
  @Override
  public void interrupt()
  {
    // Eine Unterbrechung ist nicht möglich
  }

  /**
   * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
   */
  @Override
  public boolean isInterrupted()
  {
    return false;
  }
}
