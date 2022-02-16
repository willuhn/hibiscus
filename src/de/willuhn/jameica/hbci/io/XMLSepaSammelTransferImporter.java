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
import java.util.Iterator;
import java.util.Map;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer das Hibiscus-eigene XML-Format.
 */
public class XMLSepaSammelTransferImporter extends XMLImporter
{
  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {
    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgew�hlt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgew�hlt"));

    final ClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
    Reader reader = null;
    try
    {
      reader = new XmlReader(is, new ObjectFactory() {
        @Override
        public GenericObject create(String type, String id, Map values) throws Exception
        {
          AbstractDBObject object = (AbstractDBObject) Settings.getDBService().createObject((Class<AbstractDBObject>)loader.loadClass(type),null);
          Iterator i = values.keySet().iterator();
          while (i.hasNext())
          {
            String name = (String) i.next();
            object.setAttribute(name,values.get(name));
          }
          return object;
        }
      
      });
      
      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));


      Konto konto = null;
      try
      {
        // Wir fragen das Konto grundsaetzlich manuell ab. Siehe BUGZILLA 700
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        konto = (Konto) d.open();
      }
      catch (OperationCanceledException oce)
      {
        Logger.info("import cancelled");
        return;
      }
      
      if (konto == null)
        throw new ApplicationException(i18n.tr("Kein Konto ausgew�hlt"));
      
      int created = 0;
      int error   = 0;

      SepaSammelTransfer currentTransfer = null;
      
      DBObject object = null;
      while ((object = (DBObject) reader.read()) != null)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Datensatz {0}", "" + (created+1)));
          if (created > 0 && created % 10 == 0) // nur geschaetzt
            monitor.addPercentComplete(1);
        }

        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        try
        {
          // Ist noetig, damit die Buchungen die neue ID des Transfers kriegen
          if (object instanceof SepaSammelTransfer)
          {
            currentTransfer = (SepaSammelTransfer) object;
            currentTransfer.setKonto(konto);
          }
          else
          {
            ((SepaSammelTransferBuchung)object).setSammelTransfer(currentTransfer);
          }
          
          object.store();
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
      monitor.setStatusText(i18n.tr("{0} Datens�tze erfolgreich importiert, {1} fehlerhafte �bersprungen", ""+created, ""+error));
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

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (objectType == null)
      return null;
    
    if (!SepaSammelTransfer.class.isAssignableFrom(objectType))
      return null; // Nur fuer Sammel-Auftraege anbieten - fuer alle anderen tut es die Basis-Implementierung
    
    IOFormat f = new IOFormat() {
      @Override
      public String getName()
      {
        return i18n.tr("Hibiscus-Format");
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    return new IOFormat[] { f };
  }
}
