/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVEmpfaengerImporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/05 16:42:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import org.eclipse.swt.SWTException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.CSVFile;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer CSV-Dateien.
 */
public class CSVEmpfaengerImporter implements Importer
{

  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private static Hashtable types = new Hashtable();
  
  static
  {
    Hashtable empfaenger = new Hashtable();
    empfaenger.put("kontonummer",i18n.tr("Kontonummer"));
    empfaenger.put("blz",i18n.tr("Bankleitzahl"));
    empfaenger.put("name",i18n.tr("Name des Kto-Inhabers"));
    empfaenger.put("kommentar",i18n.tr("Kommentar"));
    
    types.put(Adresse.class,empfaenger);
  
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      CSVMapping mapping = new CSVMapping(Adresse.class,(Hashtable) types.get(Adresse.class));


      monitor.setStatusText(i18n.tr("Lese Datei ein"));
      monitor.addPercentComplete(1);

      CSVFile csv = new CSVFile(is);
      if (!csv.hasNext())
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Daten"));
      
      String[] line = csv.next();
      CSVImportDialog d = new CSVImportDialog(line,mapping,CSVImportDialog.POSITION_CENTER);
      d.open();

      int created = 0;
      int error   = 0;
      boolean first = true;

      DBService service = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
      do
      {
        if (!first)
          line = csv.next();

        first = false;
        
        monitor.log(i18n.tr("Importiere Zeile {0}", ""+(1+error+created)));
        monitor.addPercentComplete(1);

        try
        {
          final Adresse a = (Adresse) service.createObject(Adresse.class,null);
          
          for (int i=0;i<line.length;++i)
          {
            String name = mapping.get(i);
            if (name == null)
              continue; // nicht zugeordnet
            a.setGenericAttribute(name,line[i]);
          }
          
          // Bevor wir das Ding speichern, pruefen wir noch, ob es diesen
          // Empfaenger bereits gibt und ueberspringen ihn
          // wir checken erstmal, ob wir den schon haben.
          DBIterator list = Settings.getDBService().createList(Adresse.class);
          list.addFilter("kontonummer = ?", new Object[]{a.getKontonummer()});
          list.addFilter("blz = ?",         new Object[]{a.getBLZ()});
          if (list.hasNext())
          {
            monitor.log("  " + i18n.tr("Kto {0}, BLZ {1} existiert bereits, überspringe Datensatz", new String[]{a.getKontonummer(),a.getBLZ()}));
            continue;
          }

          a.store();
          Application.getMessagingFactory().sendMessage(new ImportMessage() {
            public GenericObject getImportedObject() throws RemoteException
            {
              return a;
            }
          });
          created++;
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
      while (csv.hasNext());
      
      monitor.setStatusText(i18n.tr("{0} Datensätze erfolgreich importiert, {1} fehlerhafte übersprungen", new String[]{""+created,""+error}));
      monitor.addPercentComplete(1);
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      if (e instanceof SWTException)
      {
        if (e.getCause() instanceof OperationCanceledException)
        {
          Logger.info("operation cancelled");
          monitor.setStatusText(i18n.tr("Import abgebrochen"));
          monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
          return;
        }
      }
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Lesen der CSV-Datei"));
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
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
    return i18n.tr("CSV-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Adresse.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Adressen an

    IOFormat f = new IOFormat() {
      public String getName()
      {
        return i18n.tr("CSV-Format");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"*.csv","*.txt"};
      }
    };
    return new IOFormat[] { f };
  }
}

/*******************************************************************************
 * $Log: CSVEmpfaengerImporter.java,v $
 * Revision 1.1  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 ******************************************************************************/