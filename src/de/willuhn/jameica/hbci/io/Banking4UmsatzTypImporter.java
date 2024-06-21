/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Umsatzkategorien im Banking4-Format.
 */
public class Banking4UmsatzTypImporter implements Importer
{
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {
    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));

    final List<String> lines = new ArrayList<>();
    final Map<Integer,UmsatzTyp> parents = new HashMap<>();

    BufferedReader reader = null;
    try
    {
      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      String encoding = settings.getString("banking4.encoding","ISO-8859-15");
      Logger.info("banking4 encoding: " + encoding);
      
      reader = new BufferedReader(new InputStreamReader(is,encoding));
      String line = null;
      while ((line = reader.readLine()) != null)
      {
        // Zeilen, die mit einer Klammer beginnen, ignorieren wir
        // Das sind interne Vorgabekategorien wie "(nicht auswerten):Buchung wird nicht ausgewertet::exclude"
        if (line.startsWith("("))
          continue;
        lines.add(line);
      }
      
      if (lines.size() == 0)
        throw new ApplicationException(i18n.tr("Datei enthält keine Buchungen"));
      
      double factor = 100d / (double) lines.size();

      int created = 0;
      int error   = 0;
      
      final DBService service = Settings.getDBService();

      for (int i=0;i<lines.size();++i)
      {
        if (monitor != null)
          monitor.setPercentComplete((int)((i+1) * factor));
        
        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        try
        {
          final String[] cols = lines.get(i).split(":");
          if (cols == null || cols.length == 0)
            continue;
          
          final String name = StringUtils.trimToNull(cols[0]);
          if (name == null)
            continue;
          
          final int indent = cols[0].length() - name.length();
          
          final String desc = cols.length > 1 ? cols[1] : null;
          final String query = cols.length > 2 ? cols[2] : null;
          
          final UmsatzTyp typ = service.createObject(UmsatzTyp.class,null);
          typ.setName(name);
          typ.setKommentar(desc);
          typ.setPattern(query !=null ? query.replace(" ",", ") : null); // Die Suchbegriffe sind bei uns komma-separiert. In Banking4 sind es Leerzeichen
          typ.setTyp(UmsatzTyp.TYP_EGAL);
          typ.setRegex(false);
          
          // Wenn Indent-Level größer 0 ist, muss ein passendes Parent existieren
          // Wir nehmen das letzte von dem Level darüber
          if (indent > 0)
            typ.setParent(parents.get(indent-1));
          
          typ.store();
          parents.put(indent,typ);
          
          monitor.log(i18n.tr("Kategorie angelegt: {0}",cols[0]));
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
      monitor.setStatusText(i18n.tr("{0} Kategorien erfolgreich importiert, {1} fehlerhafte übersprungen", Integer.toString(created), Integer.toString(error)));
      monitor.addPercentComplete(1);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Datei"));
    }
    finally
    {
      IOUtil.close(reader);
    }
  }
  
  @Override
  public String getName()
  {
    return i18n.tr("Banking4-Format");
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!UmsatzTyp.class.equals(objectType))
      return null;
    
    IOFormat f = new IOFormat() {
      @Override
      public String getName()
      {
        return Banking4UmsatzTypImporter.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] {"*.tre"};
      }
    };
    return new IOFormat[] { f };
  }
}
