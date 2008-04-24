/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVEmpfaengerImporter.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/04/24 11:37:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.SWTException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
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
  
  private static Hashtable typeMap = new Hashtable();
  
  static
  {
    typeMap.put("kontonummer",i18n.tr("Kontonummer"));
    typeMap.put("blz",i18n.tr("Bankleitzahl"));
    typeMap.put("name",i18n.tr("Name des Kto-Inhabers"));
    typeMap.put("kommentar",i18n.tr("Kommentar"));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    ICsvListReader csv = null;
    
    try
    {
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      monitor.setStatusText(i18n.tr("Lese Datei ein"));
      monitor.addPercentComplete(1);


      CSVMapping mapping = new CSVMapping(HibiscusAddress.class,typeMap);
      csv = new CsvListReader(new InputStreamReader(new BufferedInputStream(is),mapping.getFileEncoding()),mapping.toCsvPreference());

      List line = csv.read();
      if (line == null || line.size() == 0)
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Daten"));
      
      CSVImportDialog d = new CSVImportDialog((String[])line.toArray(new String[line.size()]),mapping,CSVImportDialog.POSITION_CENTER);
      d.open();

      // Parameter aktualisieren, die der User ggf. geaendert hat
      csv.setPreferences(mapping.toCsvPreference());
      
      // Ggf. erste Zeile ueberspringen
      if (mapping.getSkipFirst())
        line = csv.read();
      
      int created       = 0;
      int error         = 0;
      HibiscusAddress a = null;
      String name       = null;
      String value      = null;

      DBService service       = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
      AddressbookService book = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      do
      {
        monitor.log(i18n.tr("Importiere Zeile {0}", ""+(1+error+created)));
        monitor.addPercentComplete(1);

        try
        {
          // Wir erzeugen aus den Daten erstmal eine Adresse. Nach der
          // koennen wir dann suchen
          a = (HibiscusAddress) service.createObject(HibiscusAddress.class,null);
          
          for (int i=0;i<line.size();++i)
          {
            name = mapping.get(i);
            if (name == null)
              continue; // nicht zugeordnet
            
            value = (String) line.get(i);
            if (value == null) value = "";
            
            // trim
            value = value.trim();

            a.setGenericAttribute(name,value);
          }
          
          if (book.contains(a) != null)
          {
            monitor.log("  " + i18n.tr("Adresse (Kto {0}, BLZ {1}) existiert bereits, überspringe Datensatz", new String[]{a.getKontonummer(),a.getBLZ()}));
            continue;
          }
          
          a.store();
          Application.getMessagingFactory().sendMessage(new ImportMessage(a));
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
      while ((line = csv.read()) != null);
      
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
      if (csv != null)
      {
        try
        {
          csv.close();
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
    if (!HibiscusAddress.class.equals(objectType))
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
 * Revision 1.7  2008/04/24 11:37:21  willuhn
 * @N BUGZILLA 304
 *
 * Revision 1.6  2007/07/16 12:48:32  willuhn
 * @B Fehler beim CSV-Import/Export von Adressen
 *
 * Revision 1.5  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.4  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.3  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.2  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.1  2006/10/05 16:42:28  willuhn
 * @N CSV-Import/Export fuer Adressen
 *
 ******************************************************************************/