/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/migration/Attic/DatabaseMigrationTask.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/10/05 17:07:05 $
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

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.DauerauftragImpl;
import de.willuhn.jameica.hbci.server.HibiscusAddressImpl;
import de.willuhn.jameica.hbci.server.KontoImpl;
import de.willuhn.jameica.hbci.server.LastschriftImpl;
import de.willuhn.jameica.hbci.server.NachrichtImpl;
import de.willuhn.jameica.hbci.server.ProtokollImpl;
import de.willuhn.jameica.hbci.server.SammelLastBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelLastschriftImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungImpl;
import de.willuhn.jameica.hbci.server.TurnusImpl;
import de.willuhn.jameica.hbci.server.UeberweisungImpl;
import de.willuhn.jameica.hbci.server.UmsatzImpl;
import de.willuhn.jameica.hbci.server.UmsatzTypImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Task zum Migrieren der Datenbank.
 */
public class DatabaseMigrationTask implements BackgroundTask
{
  protected I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  static de.willuhn.jameica.system.Settings SETTINGS = new de.willuhn.jameica.system.Settings(DatabaseMigrationTask.class);

  private boolean cancel = false;
  protected DBService source = null;
  protected DBService target = null;

  /**
   * Legt die Datenquelle fest.
   * @param source Datenquelle.
   */
  public void setSource(DBService source)
  {
    this.source = source;
  }
  
  /**
   * Legt das Datenziel fest.
   * @param target Datenziel.
   */
  public void setTarget(DBService target)
  {
    this.target = target;
  }
  
  /**
   * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
   */
  public void interrupt()
  {
    this.cancel = true;
  }

  /**
   * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
   */
  public boolean isInterrupted()
  {
    return cancel;
  }

  /**
   * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
   */
  public void run(ProgressMonitor monitor) throws ApplicationException
  {
    try
    {
      monitor.log(i18n.tr("Starte Datenmigration"));
      Logger.info("################################################");
      Logger.info("starting data migration");
      copy(TurnusImpl.class,monitor);
      copy(UmsatzTypImpl.class,monitor);


      copy(HibiscusAddressImpl.class,monitor);

      copy(KontoImpl.class,monitor);
      copy(NachrichtImpl.class,monitor);
      copy(UmsatzImpl.class,monitor);
      
      copy(DauerauftragImpl.class,monitor);
      copy(LastschriftImpl.class,monitor);
      copy(UeberweisungImpl.class,monitor);

      copy(SammelLastschriftImpl.class,monitor);
      copy(SammelLastBuchungImpl.class,monitor);

      copy(SammelUeberweisungImpl.class,monitor);
      copy(SammelUeberweisungBuchungImpl.class,monitor);

      // Die Protokolle zum Schluss.
      copy(ProtokollImpl.class,monitor);

      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setStatusText(i18n.tr("Fertig"));
    }
    catch (ApplicationException ae)
    {
      monitor.setStatusText(ae.getMessage());
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw ae;
    }
    catch (Exception e)
    {
      monitor.setStatusText(e.getMessage());
      monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      throw new ApplicationException(e);
    }
    finally
    {
      monitor.setPercentComplete(100);
    }
  }
  
  /**
   * Kann von der abgeleiteten Klasse ueberschrieben werden, um Daten zu korrigieren.
   * @param object das ggf noch zu korrigierende Objekt.
   * @param monitor Monitor.
   * @throws RemoteException
   */
  protected void fixObject(AbstractDBObject object, ProgressMonitor monitor) throws RemoteException
  {
  }
  
  /**
   * Kopiert eine einzelne Tabelle.
   * @param type Objekttyp.
   * @param monitor Monitor.
   * @throws Exception
   */
  protected void copy(Class type, ProgressMonitor monitor) throws Exception
  {
    monitor.setStatusText(i18n.tr("Kopiere " + type.getName()));
    Logger.info("  copying " + type.getName());

    long count          = 0;
    DBIterator i        = source.createList(type);
    AbstractDBObject to = null;

    while (!cancel && i.hasNext())
    {
      DBObject from = (DBObject) i.next();
      to            = (AbstractDBObject) target.createObject(type,null);

      String id = null;
      try
      {
        id = from.getID();
        to.transactionBegin();
        to.overwrite(from);
        if (++count % 100 == 0)
        {
          monitor.log(i18n.tr("  Kopierte Datensätze: {0}",""+count));
          Logger.info("  copied records: " + count);
          monitor.addPercentComplete(1);
        }
        to.setID(id);
        fixObject(to,monitor);
        to.insert();
        to.transactionCommit();
      }
      catch (Exception e)
      {
        Logger.error("unable to copy record " + type.getName() + ":" + id + ": " + BeanUtil.toString(from),e);
        if (to == null)
        {
          monitor.log(i18n.tr("Fehler beim Kopieren des Datensatzes, überspringe"));
        }
        else
        {
          try
          {
            monitor.log(i18n.tr("  Fehler beim Kopieren von [ID: {0}]: {1}, überspringe", new String[]{id,BeanUtil.toString(to)}));
            to.transactionRollback();
          }
          catch (Exception e2)
          {
            // ignore
          }
        }
      }
    }
    monitor.addPercentComplete(5);
  }
}


/*********************************************************************
 * $Log: DatabaseMigrationTask.java,v $
 * Revision 1.6  2007/10/05 17:07:05  willuhn
 * @N Jetzt aber - Migration fertig ;) ..temporaer aber noch in McKoiToH2MigrationListener deaktiviert
 *
 * Revision 1.5  2007/10/05 16:16:58  willuhn
 * @C temporaer noch deaktiviert, bis hinreichend getestet
 *
 * Revision 1.4  2007/10/05 15:55:26  willuhn
 * @B Korrigieren ueberlanger Verwendungszwecke
 *
 * Revision 1.3  2007/10/05 15:27:14  willuhn
 * @N Migration auf H2 laeuft! ;)
 *
 * Revision 1.2  2007/10/04 23:39:49  willuhn
 * @N Datenmigration McKoi->H2 (in progress)
 *
 * Revision 1.1  2007/10/04 22:07:55  willuhn
 * @N Generisches Migrationstool zum Kopieren der Daten aus einer Hibiscus-Datenbank in eine andere
 *
 **********************************************************************/