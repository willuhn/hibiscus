/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/HibiscusImporter.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/12/17 22:49:48 $
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
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.system.Application;
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
          try
          {
            Application.getMessagingFactory().sendMessage(new ImportMessage(newObject));
          }
          catch (Exception ex)
          {
            Logger.error("error while sending import message",ex);
          }
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
 * Revision 1.4  2008/12/17 22:49:48  willuhn
 * @R t o d o  tag entfernt
 *
 * Revision 1.3  2007/04/26 13:58:54  willuhn
 * @N Import-Message nach dem Import senden
 *
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