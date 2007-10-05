/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/migration/Attic/McKoiToH2MigrationTask.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/10/05 15:55:26 $
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

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.gui.internal.action.FileClose;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
      

      H2DBServiceImpl target = new H2DBServiceImpl();
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
    SETTINGS.setAttribute("migration.mckoi-to-h2",HBCI.LONGDATEFORMAT.format(new Date()));

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

  /**
   * @see de.willuhn.jameica.hbci.migration.DatabaseMigrationTask#fixObject(de.willuhn.datasource.db.AbstractDBObject, de.willuhn.util.ProgressMonitor)
   */
  protected void fixObject(AbstractDBObject object, ProgressMonitor monitor) throws RemoteException
  {
    // Wir korrigieren noch die Laenge von "Zweck2" der Umsaetze.
    // Das Feld ist zwar varchar(35). Aber Mckoi hat diese Laenge 
    // nicht ernst genommen. Da Hibiscus bis Version 1.6 versehentlich
    // in zweck2 auch die Folgezwecke eingetragen hat, koennen da
    // nun durchaus zuviele Daten drin stehen, wir verschieben sie
    // daher in das Kommentarfeld.
    if (object instanceof Umsatz)
    {
      Umsatz u = (Umsatz) object;
      String zweck2 = u.getZweck2();
      if (zweck2 == null || zweck2.length() <= 35)
        return; // Muss nicht korrigiert werden
      Logger.info(i18n.tr("  Korrigiere Verwendungszweck 2 von Umsatz [ID: {0}]",u.getID()));
      
      u.setZweck2(zweck2.substring(0,35)); // verkuerzen auf die ersten 35 Zeichen
      String rest = zweck2.substring(36);  // Der ueberhaengende Rest
      
      String comment = u.getKommentar();
      if (comment == null || comment.length() == 0)
      {
        // Wenn kein Kommentar vorhanden ist, setzen wir den ueberhaengenden Verwendungszweck da ein
        u.setKommentar(rest);
      }
      else
      {
        u.setKommentar(rest + System.getProperty("line.separator","\n") + comment);
      }
    }
    super.fixObject(object,monitor);
  }

  /**
   * Wrapper des DB-Service, damit die Identifier gross geschrieben werden.
   */
  public static class H2DBServiceImpl extends HBCIDBServiceImpl
  {
    /**
     * ct.
     * @throws RemoteException
     */
    public H2DBServiceImpl() throws RemoteException
    {
      super(DBSupportH2Impl.class.getName());
      
      // Der Konstruktor von DBSupportH2Impl hat bereits Gross-Schreibung
      // fuer HBCIDBService aktiviert - nochmal fuer die Migration
      // deaktivieren
      System.setProperty(HBCIDBServiceImpl.class.getName() + ".uppercase","false");    

      // Fuer uns selbst aktivieren wir es jedoch
      System.setProperty(H2DBServiceImpl.class.getName() + ".uppercase","true");    
    }
  }
}



/**********************************************************************
 * $Log: McKoiToH2MigrationTask.java,v $
 * Revision 1.3  2007/10/05 15:55:26  willuhn
 * @B Korrigieren ueberlanger Verwendungszwecke
 *
 * Revision 1.2  2007/10/05 15:27:14  willuhn
 * @N Migration auf H2 laeuft! ;)
 *
 * Revision 1.1  2007/10/04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 **********************************************************************/
