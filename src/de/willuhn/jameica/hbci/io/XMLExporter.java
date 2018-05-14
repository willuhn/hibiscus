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

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Daten im XML-Format.
 * Macht eigentlich nichts anderes, als die Objekte mit Java-Mitteln nach XML zu serialisieren.
 */
public class XMLExporter implements Exporter
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    Writer writer = null;

    try
    {
      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
      }

      writer = new XmlWriter(os);
      for (int i=0;i<objects.length;++i)
      {
        if (monitor != null)  monitor.setPercentComplete((int)((i) * factor));
        Object name = BeanUtil.toString(objects[i]);
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        writer.write((GenericObject)objects[i]);
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to write xml file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Daten. " + e.getMessage()));
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schliesse Export-Datei"));
      }
      try
      {
        if (writer != null)
          writer.close();
      }
      catch (Exception e) {/*useless*/}
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (objectType == null)
      return null;
    
    if (!GenericObject.class.isAssignableFrom(objectType))
      return null; // Export fuer alles moeglich, was von GenericObject abgeleitet ist

    // BUGZILLA 700
    if (SammelTransfer.class.isAssignableFrom(objectType))
      return null; // Keine Sammel-Auftraege - die muessen gesondert behandelt werden.

    if (SepaSammelTransfer.class.isAssignableFrom(objectType))
      return null; // Keine SEPA-Sammel-Auftraege - die muessen gesondert behandelt werden.
    
    return new IOFormat[]{new IOFormat() {
      public String getName()
      {
        return XMLExporter.this.getName();
      }
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"xml"};
      }
    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Hibiscus-Format");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }

}
