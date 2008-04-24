/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVUmsatzImporter.java,v $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
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
  
  private static Hashtable fieldMap = new Hashtable();
  private Hashtable kategorieCache = null;
  
  static
  {
    fieldMap.put("art",i18n.tr("Art der Buchung"));
    fieldMap.put("empfaenger_konto",i18n.tr("Gegenkonto Kto-Nummer"));
    fieldMap.put("empfaenger_blz",i18n.tr("Gegenkonto BLZ"));
    fieldMap.put("empfaenger_name",i18n.tr("Gegenkonto Kto-Inhaber"));
    fieldMap.put("betrag",i18n.tr("Betrag (im Format 000,00)"));
    fieldMap.put("zweck",i18n.tr("Verwendungszweck"));
    fieldMap.put("zweck2",i18n.tr("weiterer Verwendungszweck"));
    fieldMap.put("datum",i18n.tr("Datum (im Format (TT.MM.JJJJ)"));
    fieldMap.put("valuta",i18n.tr("Valuta (im Format (TT.MM.JJJJ)"));
    fieldMap.put("saldo",i18n.tr("Saldo (im Format 000,00)"));
    fieldMap.put("primanota",i18n.tr("Primanota"));
    fieldMap.put("customerref",i18n.tr("Kundenreferenz"));
    fieldMap.put("kommentar",i18n.tr("Kommentar"));
    fieldMap.put("umsatztyp",i18n.tr("Kategorie"));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    ICsvListReader csv = null;
    
    try
    {
      if (context == null || !(context instanceof Konto))
        throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));
      
      if (is == null)
        throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
      
      if (format == null)
        throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

      monitor.setStatusText(i18n.tr("Lese Datei ein"));
      monitor.addPercentComplete(1);


      CSVMapping mapping = new CSVMapping(Umsatz.class,fieldMap);
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
      
      int created  = 0;
      int error    = 0;
      Umsatz u     = null;
      String name  = null;
      String value = null;

      DBService service = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
      do
      {
        monitor.log(i18n.tr("Importiere Zeile {0}", ""+(1+error+created)));
        monitor.addPercentComplete(1);

        try
        {
          u = (Umsatz) service.createObject(Umsatz.class,null);
          u.setKonto((Konto) context);
          
          for (int i=0;i<line.size();++i)
          {
            name = mapping.get(i);
            if (name == null)
              continue; // nicht zugeordnet
            
            value = (String) line.get(i);
            if (value == null) value = "";
            
            // trim
            value = value.trim();
            
            // BUGZILLA 373 Sonderregel fuer Umsatz-Typ. Nicht schoen,
            // aber sonst muesste ich das direkt in UmsatzImpl machen
            if ("umsatztyp".equals(name)) {
              u.setUmsatzTyp(findUmsatzTyp(value));
              continue;
            }
            if ("betrag".equals(name)) {
              u.setBetrag(parseBetrag(value));
              continue;
            }
            if ("saldo".equals(name)) {
              u.setSaldo(parseBetrag(value));
              continue;
            }

            u.setGenericAttribute(name,value);
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
  
  /**
   * Parst einen Text als Betrag.
   * @param betrag der zu parsende Betrag.
   * @return der Betrag.
   * @throws ApplicationException
   */
  private double parseBetrag(String betrag) throws ApplicationException
  {
    try
    {
      if (betrag == null || betrag.length() == 0)
        throw new Exception();

      // Wir ersetzen alles, was nicht Zahl, Komma oder Punkt ist.
      // Damit entfernen wir automatisch alle Waehrungskennzeichen
      betrag = betrag.replaceAll("[^0-9-,\\.]","");

      // Nix mehr zum Parsen uebrig?
      if (betrag.length() == 0)
        throw new Exception();
      
      // Wenn der Text jetzt Punkt UND Komma enthaelt, entfernen wir die Punkte
      if (betrag.indexOf(".") != -1 && betrag.indexOf(",") != -1)
        betrag = betrag.replaceAll("\\.","");
      
      // Wenn jetzt immer ein Punkt drin sind, muss es ein Komma sein
      if (betrag.indexOf(".") != -1)
        betrag = betrag.replaceFirst("\\.",",");
      
      // Wenn jetzt immer noch ein Punkt drin ist, sah der Text
      // vorher so aus: 123.456.000
      // Dann entfernen wir alle Punkte
      betrag = betrag.replaceAll("\\.","");
      
      return HBCI.DECIMALFORMAT.parse(betrag).doubleValue();
    }
    catch (Exception e)
    {
      Logger.error("unable to parse string " + betrag + " as double",e);
      throw new ApplicationException(i18n.tr("Text \"{0}\" ist kein gültiger Betrag",betrag));
    }
  }
  
  /**
   * Sucht nach einem Umsatztyp anhand des Namens oder
   * legt ihn ggf selbst an.
   * @param name der Name.
   * @return der Umsatztyp.
   */
  private synchronized UmsatzTyp findUmsatzTyp(String name)
  {
    if (name == null || name.length() == 0)
      return null;
    
    try
    {
      if (kategorieCache == null)
      {
        kategorieCache = new Hashtable();
        DBIterator kategorien = Settings.getDBService().createList(UmsatzTyp.class);
        while (kategorien.hasNext())
        {
          UmsatzTyp t = (UmsatzTyp) kategorien.next();
          kategorieCache.put(t.getName().toLowerCase(),t);
        }
      }
      
      UmsatzTyp t = (UmsatzTyp) kategorieCache.get(name.toLowerCase());
      if (t != null)
        return t;
      
      // Nicht gefunden. Also neu anlegen
      t = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
      t.setName(name);
      t.store();
      kategorieCache.put(name.toLowerCase(),t);
      return t;
    }
    catch (ApplicationException ae)
    {
      Logger.error(i18n.tr("Fehler beim Anlegen der Kategorie {0}: ",name) + ae.getMessage());
    }
    catch (RemoteException re)
    {
      Logger.error("error while loading categories",re);
    }
    return null;
  }
}

/*******************************************************************************
 * $Log: CSVUmsatzImporter.java,v $
 * Revision 1.7  2008/04/24 11:37:21  willuhn
 * @N BUGZILLA 304
 *
 * Revision 1.6  2008/04/06 23:29:42  willuhn
 * @B koennte ggf. beim Import grosser Datenmengen einen OutOfMemoryError ausloesen
 *
 * Revision 1.5  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.4  2007/03/21 00:45:32  willuhn
 * @N Bug 373 - Export/Import der Umsatzkategorien zusammen mit den Umsatzen bei CSV-Format
 *
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