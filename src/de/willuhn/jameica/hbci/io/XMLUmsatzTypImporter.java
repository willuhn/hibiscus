/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.db.AbstractDBObjectNode;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * XML-Importer fuer Kategorien.
 */
public class XMLUmsatzTypImporter implements Importer
{

  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {

    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

    final ClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
    
    Reader reader = null;
    try
    {
      reader = new XmlReader(is, new ObjectFactory() {
        public GenericObject create(String type, String id, Map values) throws Exception
        {
          AbstractDBObjectNode object = (AbstractDBObjectNode) Settings.getDBService().createObject((Class<AbstractDBObject>)loader.loadClass(type),null);
          object.setID(id);
          for (Object key : values.keySet())
          {
            String name = (String) key;
            object.setAttribute(name,values.get(name));
          }
          return object;
        }
      
      });
      
      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      int created = 0;
      int error   = 0;

      Map<String,String> idMap = new HashMap<String,String>();
      
      AbstractDBObjectNode object = null;
      while ((object = (AbstractDBObjectNode) reader.read()) != null)
      {
        
        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        if (monitor != null)
        {
          monitor.log(i18n.tr("Datensatz {0}", "" + (created+1)));
          if (created > 0 && created % 10 == 0) // nur geschaetzt
            monitor.addPercentComplete(1);
        }

        try
        {
          String id = object.getID();
          object.setID(null);
          
          // Remap parent
          Object parent = object.getAttribute("parent_id");
          if (parent != null)
            object.setAttribute("parent_id",idMap.get(parent.toString()));
          
          object.store();
          idMap.put(id,object.getID());
          created++;
          try
          {
            Application.getMessagingFactory().sendMessage(new ImportMessage(object));
          }
          catch (Exception ex)
          {
            Logger.error("error while sending import message",ex);
          }
        }
        catch (ApplicationException ae)
        {
          monitor.log("  " + ae.getMessage());
          error++;
        }
        catch (Exception e)
        {
          Logger.error("unable to import line",e);
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes: {0}",e.getMessage()));
          error++;
        }
      }

      monitor.setStatusText(i18n.tr("{0} Datensätze erfolgreich importiert, {1} fehlerhafte übersprungen", new String[]{""+created,""+error}));
      monitor.setPercentComplete(100);
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der XML-Datei"));
    }
    finally
    {
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException e)
        {
          Logger.error("error while closing inputstream",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Hibiscus-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!UmsatzTyp.class.isAssignableFrom(objectType))
      return null;
    
    IOFormat f = new IOFormat() {
      public String getName()
      {
        return i18n.tr("Hibiscus-Format");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    return new IOFormat[] { f };
  }
}
