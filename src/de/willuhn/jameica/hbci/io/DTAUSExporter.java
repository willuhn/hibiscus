/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/DTAUSExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/07 22:42:00 $
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

import de.jost_net.OBanToo.Dtaus.DtausDateiWriter;
import de.willuhn.datasource.GenericObject;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Exporters fuer DTAUS-Dateien.
 */
public class DTAUSExporter extends AbstractDTAUSIO implements Exporter
{

  /**
   * ct.
   */
  public DTAUSExporter()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(de.willuhn.datasource.GenericObject[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(GenericObject[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Ziel für die Datei angegeben"));

    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Format angegeben"));

    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu exportierenden Daten angegeben"));

    if (!(objects instanceof Transfer[]))
      throw new ApplicationException(i18n.tr("Die zu exportierenden Daten enthalten keine HBCI-Aufträge"));
      
    try
    {
      int success = 0;
      
      DtausDateiWriter writer = new DtausDateiWriter(os);
      writer.writeASatz();
      for (int i=0;i<objects.length;++i)
      {
        writer.writeCSatz();
      }
      writer.writeESatz();
      monitor.setStatusText(i18n.tr("{0} Aufträge erfolgreich exportiert",""+success));
      os = null; // wird vokm DTAUSWriter geschlossen
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Export abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Export der Daten"),e);
    }
    finally
    {
      // Outputstream schliessen, falls das noch nicht geschehen ist
      if (os != null)
      {
        try
        {
          os.close();
        }
        catch (Throwable t)
        {
          Logger.error("unable to close file",t);
        }
      }
    }
  }
}


/**********************************************************************
 * $Log: DTAUSExporter.java,v $
 * Revision 1.1  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 **********************************************************************/