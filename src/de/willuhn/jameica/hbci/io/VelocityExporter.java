/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/VelocityExporter.java,v $
 * $Revision: 1.15 $
 * $Date: 2009/03/10 23:51:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Exporters, welcher das Velocity-Framework nutzt.
 */
public class VelocityExporter implements Exporter
{

  private File templateDir        = null;

  private HashMap formats         = new HashMap();

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public VelocityExporter()
  {
    super();
    Logger.info("init velocity export engine");
    
    AbstractPlugin p = Application.getPluginLoader().getPlugin(HBCI.class);
    this.i18n = p.getResources().getI18N();
    this.templateDir = new File(p.getManifest().getPluginDir() + File.separator + "lib","velocity");
  }

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

    context.put("datum",        new Date());
    context.put("charset",      System.getProperty("file.encoding")); // BUGZILLA 328
    context.put("dateformat",   HBCI.DATEFORMAT);
    context.put("decimalformat",HBCI.DECIMALFORMAT);
    context.put("objects",      objects);
    
//    String icon = getIcon();
//    if (icon != null)
//      context.put("icon",icon);

    BufferedWriter writer = null;
    try
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
        monitor.addPercentComplete(4);
      }
      writer = new BufferedWriter(new OutputStreamWriter(os));

      Template template = Velocity.getTemplate(((VelocityFormat)format).getTemplate().getName(),"ISO-8859-15");
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
  
//  /**
//   * Liefert ein auf dem Report anzuzeigendes Icon als Base64.
//   * @return Icon oder NULL, wenn es nicht geladen werden konnte.
//   */
//  private String getIcon()
//  {
//    InputStream is = null;
//    try
//    {
//      ByteArrayOutputStream bos = new ByteArrayOutputStream();
//      is = Application.getClassLoader().getResourceAsStream("img/hibiscus-icon-64x64.png");
//      int read = 0;
//      byte[] buf = new byte[1024];
//      while ((read = is.read(buf)) != -1)
//        bos.write(buf,0,read);
//      return Base64.encode(bos.toByteArray());
//    }
//    catch (Exception e)
//    {
//      Logger.write(Level.INFO,"unable to read icon, skipping",e);
//    }
//    return null;
//  }
  
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
    FileFinder finder = new FileFinder(this.templateDir);
    String cn = type.getName().replaceAll("\\.","\\\\.");
    finder.matches(cn + ".*?\\.vm$");

    File[] found = finder.findRecursive();
    
    ArrayList l = new ArrayList();
    for (int i=0;i<found.length;++i)
    {
      final File ef = found[i];
      Logger.info("  found template: " + ef.getAbsolutePath());

      String name = ef.getName();
      name = name.replaceAll(cn + "\\.",""); // Klassenname und Punkt dahinter entfernen
      int dot = name.indexOf(".");
      if (dot == -1)
        continue;
      String ext = name.substring(0,dot);
      if (ext == null || ext.length() == 0)
        continue;
      
      l.add(new VelocityFormat(ext,ef));
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
    private File template = null;
    
    /**
     * @param extension
     * @param f
     */
    public VelocityFormat(String extension,File f)
    {
      this.extension = extension;
      this.template = f;
    }
    
    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      return extension.toUpperCase() + "-" + i18n.tr("Format");
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

}


/**********************************************************************
 * $Log: VelocityExporter.java,v $
 * Revision 1.15  2009/03/10 23:51:31  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.14  2009/03/02 11:42:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2009/03/02 11:41:51  willuhn
 * @N title in HTML-Exports
 *
 * Revision 1.12  2009/03/01 23:37:03  willuhn
 * @C Templates sollten explizit mit Latin1-Encoding gelesen werden, da sie von mir in diesem Encoding erstellt wurden
 *
 * Revision 1.11  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.10  2006/11/15 00:36:59  willuhn
 * @B Bug 326
 *
 * Revision 1.9  2006/09/05 13:56:33  willuhn
 * @B Class#getClass() allways returns "java.lang.Class" ;)
 *
 * Revision 1.8  2006/03/27 22:36:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.6  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.5  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.4  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.3  2006/01/02 17:38:12  willuhn
 * @N moved Velocity to Jameica
 *
 * Revision 1.2  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.2  2005/06/06 10:37:01  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/