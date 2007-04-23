/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVEmpfaengerImporter.java,v $
 * $Revision: 1.5 $
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

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Hashtable;

import org.eclipse.swt.SWTException;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.CSVFile;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Address;
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
  
  private static Hashtable types = new Hashtable();
  
  static
  {
    Hashtable empfaenger = new Hashtable();
    empfaenger.put("kontonummer",i18n.tr("Kontonummer"));
    empfaenger.put("blz",i18n.tr("Bankleitzahl"));
    empfaenger.put("name",i18n.tr("Name des Kto-Inhabers"));
    empfaenger.put("kommentar",i18n.tr("Kommentar"));
    
    types.put(Address.class,empfaenger);
  
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      CSVMapping mapping = new CSVMapping(Address.class,(Hashtable) types.get(Address.class));


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

      DBService service       = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
      AddressbookService book = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      do
      {
        if (!first)
          line = csv.next();

        first = false;
        
        monitor.log(i18n.tr("Importiere Zeile {0}", ""+(1+error+created)));
        monitor.addPercentComplete(1);

        try
        {
          // Wir erzeugen aus den Daten erstmal eine Adresse. Nach der
          // koennen wir dann suchen
          final HibiscusAddress a = (HibiscusAddress) service.createObject(HibiscusAddress.class,null);
          
          for (int i=0;i<line.length;++i)
          {
            String name = mapping.get(i);
            if (name == null)
              continue; // nicht zugeordnet
            a.setGenericAttribute(name,line[i]);
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
    if (!Address.class.equals(objectType))
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