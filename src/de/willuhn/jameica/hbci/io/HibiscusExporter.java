/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/HibiscusExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/12/01 01:28:16 $
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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Daten im Binaer-Format.
 * Macht eigentlich nichts anderes, als die Objekte in einen ObjectOutputStream zu schreiben.
 */
public class HibiscusExporter extends AbstractHibiscusIO implements Exporter
{
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(de.willuhn.datasource.GenericObject[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(GenericObject[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    ObjectOutputStream o = null;
    try
    {
      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
      }

      o = new ObjectOutputStream(os);
      for (int i=0;i<objects.length;++i)
      {
        if (monitor != null)  monitor.setPercentComplete((int)((i) * factor));
        Object name = objects[i].getAttribute(objects[i].getPrimaryAttribute());
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        o.writeObject(objects[i]);
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to serialize objects",e);
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Fehler beim Export der Daten"));
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
      }
    }
    finally
    {
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Schliesse Export-Datei"));
      }
      try
      {
        os.close();
      }
      catch (Exception e) {/*useless*/}
    }
  }
}


/*********************************************************************
 * $Log: HibiscusExporter.java,v $
 * Revision 1.1  2006/12/01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/