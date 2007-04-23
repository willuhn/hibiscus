/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/HibiscusImporter.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/23 18:07:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Hibiscus-Objekte.
 */
public class HibiscusImporter extends AbstractHibiscusIO implements Importer
{

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    ObjectInputStream ois = null;
    try
    {
      // TODO: Mit der verflucht heissen Nadel gestrickt! ;)
      ois = new ObjectInputStream(is);
      AbstractDBObject current = null;
      DBService service = Settings.getDBService();
      while(true)
      {
        try
        {
          try
          {
            current = (AbstractDBObject) ois.readObject();
          }
          catch (EOFException e)
          {
            break;
          }
          if (current == null)
            break;
          Object name = BeanUtil.toString(current);
          if (name != null && monitor != null)
            monitor.log(i18n.tr("Importiere Datensatz {0}",name.toString()));
          monitor.addPercentComplete(1);
          AbstractDBObject newObject = (AbstractDBObject) service.createObject(current.getClass(),null);
          newObject.overwrite(current);
          newObject.store();
        }
        catch (Exception e)
        {
          Logger.error("unable to read object",e);
          monitor.log("  " + i18n.tr("Fehler beim Imortieren des Datensatzes"));
        }
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to serialize objects",e);
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Fehler beim Import der Daten"));
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      }
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schliesse Import-Datei"));
      }
      try
      {
        ois.close();
      }
      catch (Exception e) {/*useless*/}
    }
  }

}


/*********************************************************************
 * $Log: HibiscusImporter.java,v $
 * Revision 1.2  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2006/12/01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/