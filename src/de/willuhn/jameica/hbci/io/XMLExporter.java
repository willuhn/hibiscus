/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/XMLExporter.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/02/13 14:17:01 $
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
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
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

    return new IOFormat[]{new IOFormat() {
      public String getName()
      {
        return i18n.tr("XML-Format");
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
    return i18n.tr("XML-Format");
  }

}


/*********************************************************************
 * $Log: XMLExporter.java,v $
 * Revision 1.4  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 * Revision 1.3  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 * Revision 1.2  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.1  2006/12/01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/