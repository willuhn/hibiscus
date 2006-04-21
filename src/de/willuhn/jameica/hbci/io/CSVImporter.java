/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVImporter.java,v $
 * $Revision: 1.1.2.1 $
 * $Date: 2006/04/21 09:15:10 $
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.io.CSVFile;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
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
public class CSVImporter // implements Importer
{

  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private static Hashtable types = new Hashtable();
  
  static
  {
    Hashtable umsatz = new Hashtable();
    umsatz.put("empfaenger_konto",i18n.tr("Gegenkonto Kto-Nummer"));
    umsatz.put("empfaenger_blz",i18n.tr("Gegenkonto BLZ"));
    umsatz.put("empfaenger_name",i18n.tr("Gegenkonto Kto-Inhaber"));
    umsatz.put("betrag",i18n.tr("Betrag"));
    umsatz.put("zweck",i18n.tr("Verwendungszweck"));
    umsatz.put("tweck2",i18n.tr("weiterer Verwendungszweck"));
    umsatz.put("datum",i18n.tr("Datum"));
    umsatz.put("valuta",i18n.tr("Valuta"));
    umsatz.put("saldo",i18n.tr("Saldo"));
    umsatz.put("primanota",i18n.tr("Primanota"));
    umsatz.put("art",i18n.tr("Art der Buchung"));
    umsatz.put("customerref",i18n.tr("Kundenreferenz"));
    umsatz.put("kommentar",i18n.tr("Kommentar"));
    
    types.put(Umsatz.class,umsatz);
  
  }

  /**
   * ct.
   */
  public CSVImporter()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
//      if (context == null)
//        throw new ApplicationException(i18n.tr("Art der zu importierenden Daten nicht ausgewählt"));
      
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      // TODO die Art der zu importierenden Daten ist nocht nicht konfigurierbar
      CSVMapping mapping = new CSVMapping(Umsatz.class,(Hashtable) types.get(Umsatz.class));


      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Lese Datei ein"));
        monitor.addPercentComplete(1);
      }

      CSVFile csv = new CSVFile(is);
      if (!csv.hasNext())
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Daten"));
      
      String[] line = csv.next();
      CSVImportDialog d = new CSVImportDialog(line,mapping,CSVImportDialog.POSITION_CENTER);
      d.open();

      int created = 0;
      int error   = 0;

      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("{0} Datensätze importiert, {1} übersprungen (fehlerhaft)", new String[]{""+created,""+error}));
        monitor.addPercentComplete(1);
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
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
 * $Log: CSVImporter.java,v $
 * Revision 1.1.2.1  2006/04/21 09:15:10  willuhn
 * @B MT940-Import wieder aktiviert
 *
 * Revision 1.1  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 ******************************************************************************/