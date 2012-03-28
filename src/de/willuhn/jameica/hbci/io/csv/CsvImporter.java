/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/csv/CsvImporter.java,v $
 * $Revision: 1.5 $
 * $Date: 2012/03/28 22:47:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWTException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVImportDialog;
import de.willuhn.jameica.hbci.io.IOFormat;
import de.willuhn.jameica.hbci.io.Importer;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer CSV-Daten.
 */
public class CsvImporter implements Importer
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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

      if (!(format instanceof MyIOFormat))
        throw new ApplicationException(i18n.tr("Das Datei-Format ist unbekannt"));
      
      Format f = ((MyIOFormat)format).format;


      monitor.setStatusText(i18n.tr("Öffne Datei"));
      monitor.addPercentComplete(1);
      
      
      // Wir lesen die Datei einmal komplett in einen Buffer.
      // Grund: Sie soll als Preview im Spalten-Zuordnungsdialog
      // angezeigt werden. Falls der User das Encoding der
      // Datei im Dialog aendert, muss sie anschliessend neu
      // mit dem geaenderten Encoding gelesen werden koennen.
      byte[] data = copy(is);
      if (data == null || data.length == 0)
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Daten"));

      CSVImportDialog d = new CSVImportDialog(data,f,CSVImportDialog.POSITION_CENTER);
      Profile p = (Profile) d.open();

      CsvPreference prefs = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
      String sep = p.getSeparatorChar();
      String quo = p.getQuotingChar();
      if (sep != null && sep.length() == 1) prefs.setDelimiterChar(sep.charAt(0));
      if (quo != null && quo.length() == 1) prefs.setQuoteChar(quo.charAt(0));
      ICsvListReader csv = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(data),p.getFileEncoding()),prefs);

      List<String> line = csv.read();
      
      // Ggf. erste Zeilen ueberspringen
      for (int i=0;i<p.getSkipLines();++i)
      {
        line = csv.read();
      }

      // Nochmal checken, ob wir nach dem Ueberspringen ueberhaupt noch Zeilen uebrig haben
      if (line == null || line.size() == 0)
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine weiteren Daten"));

      int created       = 0;
      int error         = 0;
      int skipped       = 0;
      DBObject object   = null;
      String value      = null;
      List<Column> cols = p.getColumns();
      ImportListener l  = f.getImportListener();

      DBService service = (DBService) Application.getServiceFactory().lookup(HBCI.class,"database");
      do
      {
        monitor.log(i18n.tr("Importiere Zeile {0}",Integer.toString(csv.getLineNumber())));
        monitor.addPercentComplete(1);

        try
        {
          object = service.createObject(f.getType(),null);
          
          // Spalten zuordnen
          Map<String, Object> values = new HashMap<String,Object>();
          for (int i=0;i<line.size();++i)
          {
            Column column = null;
            for (Column c:cols)
            {
              if (c.getColumn() == i)
              {
                column = c;
                break;
              }
            }
            
            if (column == null)
              continue; // Spalte nicht zugeordnet
            
            // Checken, ob in der Spalte was steht
            value = line.get(i);
            if (value == null) 
              continue;
            value = value.trim();
            if (value.length() == 0)
              continue;
            
            try
            {
              // Werte zwischenspeichern
              Object prev = values.get(column.getProperty());
              values.put(column.getProperty(),column.getSerializer().unserialize(prev,value));
            }
            catch (Exception e)
            {
              Logger.error("unable to unserialize " + column.getProperty() + " for line " + csv.getLineNumber() + ", value: " + value,e);
              monitor.log("  " + i18n.tr("Ungültiger Wert \"{0}\" in Spalte \"{1}\": {2}",value,column.getName(),e.getMessage()));
            }
          }
          
          // beforeSet-Listener ausloesen
          if (l != null)
          {
            ImportEvent e = new ImportEvent();
            e.context = context;
            e.data = values;
            try
            {
              l.beforeSet(e);
            }
            catch (OperationCanceledException oce)
            {
              skipped++;
              String msg = oce.getMessage();
              if (msg != null && msg.length() > 0)
                monitor.log("  " + msg);
              continue;
            }
          }
          

          // Werte in die Bean uebernehmen
          Iterator<String> it = values.keySet().iterator();
          while (it.hasNext())
          {
            String name = it.next();
            Object o = values.get(name);
            try
            {
              BeanUtil.set(object,name,o);
            }
            catch (Exception e)
            {
              Logger.error("unable to apply property " + name + " for line " + csv.getLineNumber() + ", value: " + o,e);
              String[] s = new String[]{value,
                                        name,
                                        e.getMessage()};
              monitor.log("  " + i18n.tr("Ungültiger Wert \"{0}\" in Spalte \"{1}\": {2}",s));
            }
          }
          
          
          
          
          if (l != null)
          {
            ImportEvent e = new ImportEvent();
            e.context = context;
            e.data = object;
            try
            {
              l.beforeStore(e);
            }
            catch (OperationCanceledException oce)
            {
              skipped++;
              String msg = oce.getMessage();
              if (msg != null && msg.length() > 0)
                monitor.log("  " + msg);
              continue;
            }
          }
          
          object.store();
          Application.getMessagingFactory().sendMessage(new ImportMessage(object));
          created++;
        }
        catch (Exception e)
        {
          if (!(e instanceof ApplicationException))
            Logger.error("unable to import line",e);

          monitor.log("  " + i18n.tr("Fehler in Zeile {0}: {1}",new String[]{Integer.toString(csv.getLineNumber()),e.getMessage()}));
          error++;
        }
      }
      while ((line = csv.read()) != null);

      // Fertig.
      monitor.setStatusText(i18n.tr("{0} importiert, {1} fehlerhaft, {2} übersprungen", new String[]{Integer.toString(created),Integer.toString(error),Integer.toString(skipped)}));
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      // Ich bin mir nicht sicher, ob das hier ueberhaupt noch noetig ist
      if (e instanceof SWTException)
      {
        if (e.getCause() instanceof OperationCanceledException)
        {
          Logger.info("operation cancelled");
          monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
          monitor.setStatusText(i18n.tr("Import abgebrochen"));
          return;
        }
      }
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Lesen der CSV-Datei"));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir checken, fuer welche Datentypen wir einen CSV-Treiber haben
    List<IOFormat> formats = new ArrayList<IOFormat>();
    try
    {
      ClassFinder finder = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader().getClassFinder();
      Class<Format>[] classes = finder.findImplementors(Format.class);
      for (Class<Format> c:classes)
      {
        try
        {
          Format f = c.newInstance();
          Class type = f.getType();
          if (type == null)
          {
            Logger.warn("csv format " + c.getName() + " supports no type, skipping");
            continue;
          }
          
          if (type.equals(objectType))
            formats.add(new MyIOFormat(f));
        }
        catch (Exception e)
        {
          Logger.error("unable to load " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e2)
    {
      Logger.error("no csv formats found");
    }

    return formats.toArray(new IOFormat[formats.size()]);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("CSV-Format");
  }

  
  /**
   * Kopiert einen Stream in einen anderen.
   * @param is Datenquelle.
   * Der Stream wird danach gleich geschlossen.
   * @return Kopie des Streams.
   * @throws IOException
   */
  private static byte[] copy(InputStream is) throws IOException
  {
    BufferedInputStream bis = null;
    try
    {
      bis = new BufferedInputStream(is);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      
      byte[] buf = new byte[4096];
      int read   = 0;
      while ((read = is.read(buf)) != -1)
      {
        if (read > 0) // Nur schreiben, wenn wirklich was gelesen wurde
          bos.write(buf,0,read);
      }
      return bos.toByteArray();
    }
    finally
    {
      bis.close();
    }
  }
  
  
  /**
   * Unser IO-Format.
   */
  private class MyIOFormat implements IOFormat
  {
    private Format format = null;
    
    /**
     * ct.
     * @param f der zugehoerige Treiber.
     */
    private MyIOFormat(Format f)
    {
      this.format = f;
    }
    
    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      return CsvImporter.this.getName();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
     */
    public String[] getFileExtensions()
    {
      return new String[]{"*.csv","*.txt"};
    }
  }

}



/**********************************************************************
 * $Log: CsvImporter.java,v $
 * Revision 1.5  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2011-06-12 22:10:06  willuhn
 * @B BUGZILLA 1053
 *
 * Revision 1.3  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 * Revision 1.2  2010/03/16 11:14:53  willuhn
 * @B Format-Implementierungen nicht cachen
 *
 * Revision 1.1  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/