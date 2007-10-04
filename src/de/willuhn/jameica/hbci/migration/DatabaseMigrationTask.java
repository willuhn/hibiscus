/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/migration/Attic/DatabaseMigrationTask.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/04 22:07:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.migration;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Task zum Migrieren der Datenbank.
 */
public class DatabaseMigrationTask implements BackgroundTask
{
  private boolean cancel = false;
  private HBCIDBService source = null;
  private HBCIDBService target = null;

  /**
   * ct.
   * @param source Datenquelle.
   * @param target Datenziel.
   */
  public DatabaseMigrationTask(HBCIDBService source, HBCIDBService target)
  {
    this.source = source;
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
      monitor.setStatusText("Fertig");
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
   * Kopiert eine einzelne Tabelle.
   * @param type Objekttyp.
   * @param monitor Monitor.
   * @throws Exception
   */
  private void copy(Class type, ProgressMonitor monitor) throws Exception
  {
    monitor.setStatusText("Kopiere " + type.getName());

  long count = 0;
  
//    monitor.log("  Zieltabelle leeren");
//    DBIterator existing = target.createList(type);
//    while (existing.hasNext())
//    {
//      DBObject ex = (DBObject) existing.next();
//      ex.transactionBegin();
//      ex.delete();
//      ex.transactionCommit();
//      count++;
//    }
//    monitor.log("  Gelöschte Datensätze: " + count);
//
    DBIterator i = source.createList(type);
    
    AbstractDBObject to = null;

    count = 0;

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
        if (++count % 1000 == 0)
        {
          monitor.log("  Kopierte Datensätze: " + count);
          monitor.addPercentComplete(1);
        }
        to.setID(id);
        to.insert();
        to.transactionCommit();
      }
      catch (Exception e)
      {
        Logger.error("unable to copy record",e);
        if (to == null)
        {
          monitor.log("Fehler beim Kopieren des Datensatzes, überspringe");
        }
        else
        {
          try
          {
            monitor.log("  Fehler beim Kopieren von [ID: " + id + "]: " + BeanUtil.toString(to) + ", überspringe");
            to.transactionRollback();
          }
          catch (Exception e2)
          {
            // ignore
          }
        }
      }
    }
    monitor.addPercentComplete(10);
  }
}


/*********************************************************************
 * $Log: DatabaseMigrationTask.java,v $
 * Revision 1.1  2007/10/04 22:07:55  willuhn
 * @N Generisches Migrationstool zum Kopieren der Daten aus einer Hibiscus-Datenbank in eine andere
 *
 **********************************************************************/