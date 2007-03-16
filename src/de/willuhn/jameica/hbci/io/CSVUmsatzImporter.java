/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVUmsatzImporter.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/16 14:40:02 $
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
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.CSVFile;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer CSV-Dateien.
 */
public class CSVUmsatzImporter implements Importer
{

  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private static Hashtable types = new Hashtable();
  
  static
  {
    Hashtable umsatz = new Hashtable();
    umsatz.put("empfaenger_konto",i18n.tr("Gegenkonto Kto-Nummer"));
    umsatz.put("empfaenger_blz",i18n.tr("Gegenkonto BLZ"));
    umsatz.put("empfaenger_name",i18n.tr("Gegenkonto Kto-Inhaber"));
    umsatz.put("betrag",i18n.tr("Betrag (im Format 000,00)"));
    umsatz.put("zweck",i18n.tr("Verwendungszweck"));
    umsatz.put("zweck2",i18n.tr("weiterer Verwendungszweck"));
    umsatz.put("datum",i18n.tr("Datum (im Format (TT.MM.JJJJ)"));
    umsatz.put("valuta",i18n.tr("Valuta (im Format (TT.MM.JJJJ)"));
    umsatz.put("saldo",i18n.tr("Saldo (im Format 000,00)"));
    umsatz.put("primanota",i18n.tr("Primanota"));
    umsatz.put("art",i18n.tr("Art der Buchung"));
    umsatz.put("customerref",i18n.tr("Kundenreferenz"));
    umsatz.put("kommentar",i18n.tr("Kommentar"));
    
    types.put(Umsatz.class,umsatz);
  
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      if (context == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));
      
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      CSVMapping mapping = new CSVMapping(Umsatz.class,(Hashtable) types.get(Umsatz.class));


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
          final Umsatz u = (Umsatz) service.createObject(Umsatz.class,null);
          u.setKonto((Konto) context);
          
          for (int i=0;i<line.length;++i)
          {
            String name = mapping.get(i);
            if (name == null)
              continue; // nicht zugeordnet
            u.setGenericAttribute(name,line[i]);
          }
          u.setChangedByUser();
          u.store();
          Application.getMessagingFactory().sendMessage(new ImportMessage(u));
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
    if (!Umsatz.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Umsaetze an

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
 * $Log: CSVUmsatzImporter.java,v $
 * Revision 1.3  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.2  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.1  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.3  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 * Revision 1.2  2006/04/20 08:44:21  willuhn
 * @C s/Childs/Children/
 *
 * Revision 1.1  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 ******************************************************************************/