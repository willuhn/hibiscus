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

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.VelocityService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Exporters, welcher das Velocity-Framework nutzt.
 */
public class VelocityExporter implements Exporter
{
  private final static Settings settings = new Settings(VelocityExporter.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Map<Class,IOFormat[]> formats  = new HashMap<Class,IOFormat[]>();
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Ziel für die Datei angegeben"));

    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Format angegeben"));

    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu exportierenden Umsätze angegeben"));

    if (monitor != null)
    {
      monitor.setStatusText(i18n.tr("Bereite Template vor"));
      monitor.addPercentComplete(1);
    }
    Logger.debug("preparing velocity context");
    VelocityContext context = new VelocityContext();

    String encoding = settings.getString("file.encoding",System.getProperty("file.encoding")); // BUGZILLA 1358
    Logger.info("used encoding: " + encoding);

    // Ob wir den gesamten Verwendungszweck exportieren, entnehmen wir dem Setting "usage.display.all"
    // Heisst: Die Verwendungszwecke werden genau in der Form exportiert, in der sie derzeit auch
    // angezeigt werden. Das erspart diese missverstaendliche Option "Im Verwendungszweck "SVWZ+" extrahieren"
    Exporter.SESSION.put("usage.display.all",Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings().getBoolean("usage.display.all",true));

    context.put("datum",         new Date());
    context.put("charset",       encoding); // BUGZILLA 328
    context.put("dateformat",    HBCI.DATEFORMAT);
    context.put("dateutil",      new DateUtil());
    context.put("longdateformat",HBCI.LONGDATEFORMAT);
    context.put("decimalformat", HBCI.DECIMALFORMAT);
    context.put("objects",       objects);
    context.put("filter",        new Filter());
    context.put("session",       Exporter.SESSION);

    BufferedWriter writer = null;
    try
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
        monitor.addPercentComplete(4);
      }
      writer = new BufferedWriter(new OutputStreamWriter(os,encoding));

      VelocityService service = (VelocityService) Application.getBootLoader().getBootable(VelocityService.class);
      VelocityEngine engine = service.getEngine(HBCI.class.getName());
      if (engine == null)
        throw new Exception("velocity engine not found");

      Template template = engine.getTemplate(((VelocityFormat)format).getTemplate().getName(),"ISO-8859-15");
      template.merge(context,writer);
    }
    catch (Exception e)
    {
      Logger.error("error while writing into export file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Schreiben in die Export-Datei"));
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schliesse Export-Datei"));
        monitor.addPercentComplete(1);
      }
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Velocity-Export");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class type)
  {
    if (type == null)
      return new IOFormat[0];
    
    IOFormat[] loaded = (IOFormat[]) this.formats.get(type);
    if (loaded != null)
      return loaded;

    Logger.info("looking velocity templates for object type " + type);
    
    File dir = new File(Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getPluginDir() + File.separator + "lib","velocity");
    FileFinder finder = new FileFinder(dir);
    String cn = type.getName().replace(".","\\."); // "." gegen "\." ersetzen (Escaping fuer folgenden Regex)
    cn = cn.replace("$","\\$"); // Fuer Inner Classes
    finder.matches(cn + ".*?\\.vm$");

    File[] found = finder.findRecursive();
    
    ArrayList<VelocityFormat> l = new ArrayList<>();
    for (final File ef : found)
    {
      Logger.info("  found template: " + ef.getAbsolutePath());

      String name = ef.getName();
      name = name.replaceAll(cn,""); // Klassenname entfernen
      
      // Checken, ob wir eine Variation haben
      String variant = null;
      if (name.startsWith("-"))
        variant = name.substring(1,name.indexOf("."));

      // Punkt abschneiden
      name = name.substring(name.indexOf(".")+1);

      int dot = name.indexOf(".");
      if (dot == -1)
        continue;
      
      String ext = name.substring(0,dot);
      if (ext == null || ext.length() == 0)
        continue;
      
      l.add(new VelocityFormat(ext,variant,ef));
    }
    
    loaded = (IOFormat[]) l.toArray(new IOFormat[l.size()]);
    this.formats.put(type,loaded);
    return loaded;
    
  }
  
  /**
   * Hilfsklase, die das IOFormat implementiert.
   */
  public class VelocityFormat implements IOFormat
  {
    private String extension = null;
    private String variant   = null;
    
    private File template = null;
    
    /**
     * ct.
     * @param extension die Datei-Endung.
     * @param variant optionaler Prefix fuer eine Variation.
     * @param f das Template.
     */
    public VelocityFormat(String extension, String variant, File f)
    {
      this.extension = extension;
      this.variant   = variant;
      this.template  = f;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      String name = extension.toUpperCase() + "-" + i18n.tr("Format");
      if (variant != null && variant.length() > 0)
        name += ": " + i18n.tr(variant);
      return name;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
     */
    public String[] getFileExtensions()
    {
      return new String[]{extension};
    }
    
    /**
     * Liefert das Template-File.
     * @return Template-File.
     */
    public File getTemplate()
    {
      return this.template;
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }
  
  /**
   * Hilfsklasse fuer weitere Datumsfunktionen.
   */
  public class DateUtil
  {
    /**
     * Liefert ein neues Datumsformat mit dem angegebenen Format.
     * @param format das gewuenschte Format. Z.B. "yyyy-MM-dd".
     * @return das Datumsformat.
     */
    public DateFormat getFormat(String format)
    {
      format = StringUtils.trimToNull(format);
      if (format == null)
      {
        Logger.warn("no date format given, fallback to default format");
        return HBCI.DATEFORMAT;
      }
      
      try
      {
        return new SimpleDateFormat(format);
      }
      catch (Exception e)
      {
        Logger.error("invalid date format: " + format + " - fallback to default format",e);
      }
      return HBCI.DATEFORMAT;
    }
  }
  
  /**
   * Hilfsklasse zum Escapen von Strings in der CSV-Datei.
   */
  public class Filter
  {
    /**
     * Escaped den angegebenen String fuer CSV.
     * @param s der zu escapende String.
     * @return der escapte String.
     */
    public String escape(String s)
    {
      if (StringUtils.isEmpty(s))
        return s;

      // Double-Quote mit Double-Quote escapen
      // Siehe https://tools.ietf.org/html/rfc4180#section-2, Absatz 7
      // BUGZILLA 1336
      s = s.replace("\"","\"\"");
      
      // Zeilenumbrueche gegen Leerzeichen ersetzen
      s = s.replaceAll("[\n\r]"," ");
      
      return s;
    }
  }

}
