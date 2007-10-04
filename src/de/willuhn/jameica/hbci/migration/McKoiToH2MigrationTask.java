/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/migration/Attic/McKoiToH2MigrationTask.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/04 23:39:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.migration;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;


/**
 * Migration von McKoi nach H2.
 */
public class McKoiToH2MigrationTask extends DatabaseMigrationTask
{
  /**
   * @see de.willuhn.jameica.hbci.migration.DatabaseMigrationTask#run(de.willuhn.util.ProgressMonitor)
   */
  public void run(ProgressMonitor monitor) throws ApplicationException
  {
    // Checken, ob die Migration schon lief
    if (SETTINGS.getString("migration.mckoi-to-h2",null) != null)
      throw new ApplicationException(i18n.tr("Datenmigration bereits durchgeführt"));
    
    try
    {
      setSource(Settings.getDBService());
      
      HBCIDBService target = new HBCIDBServiceImpl(DBSupportH2Impl.class.getName());
      target.start();
      target.install();
      
      setTarget(target);
    }
    catch (RemoteException re)
    {
      monitor.setStatusText(re.getMessage());
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw new ApplicationException(re);
    }
    super.run(monitor);

    // Datum der Migration speichern
    SETTINGS.setAttribute("migration.mckoi-to-h2",HBCI.DATEFORMAT.format(new Date()));

    // Datenbank-Treiber umstellen
    HBCIDBService.SETTINGS.setAttribute("database.driver",DBSupportH2Impl.class.getName());
    
    // User ueber Neustart benachrichtigen
    String text = i18n.tr("Datenmigration erfolgreich beendet.\nHibiscus wird nun beendet. Starten Sie die Anwendung anschließend bitte neu.");
    try
    {
      Application.getCallback().notifyUser(text);
    }
    catch (Exception e)
    {
      Logger.error("unable to notify user about restart",e);
    }
    
    // Hibiscus beenden
    new FileClose().handleAction(null);
  }
}


/**********************************************************************
 * $Log: McKoiToH2MigrationTask.java,v $
 * Revision 1.1  2007/10/04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 **********************************************************************/
