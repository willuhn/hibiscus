/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/IORegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/08 16:48:54 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Ueber diese Klasse koennen alle verfuegbaren Export-und Import Formate abgerufen werden.
 */
public class IORegistry
{

  // Liste der Export-Filter
  private static Exporter[] exporters = null;

  /**
   * Initialisiert die Registry.
   */
  public static synchronized void init()
  {
    if (exporters != null)
      return; // wurde schonmal aufgerufen

    try
    {
      Logger.info("looking for installed export filters");
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class[] list = finder.findImplementors(Exporter.class);
      if (list == null || list.length == 0)
        throw new ClassNotFoundException();

      // Initialisieren der Exporter
      exporters = new Exporter[list.length];

      for (int i=0;i<list.length;++i)
      {
        try
        {
          Logger.info("trying to load " + list[i].getName());
          exporters[i] = (Exporter) list[i].newInstance();
          Logger.info("loaded successfully");
        }
        catch (Exception e)
        {
          Logger.error("error while loading export filter " + list[i].getName(),e);
        }
      }

    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no export filters found");
      exporters = new Exporter[0];
    }
  }

  /**
   * Liefert eine Liste aller verfuegbaren Export-Formate.
   * @return Export-Filter.
   */
  public static Exporter[] getExporters()
  {
    if (exporters == null)
      init();

    return exporters;
  }
}


/**********************************************************************
 * $Log: IORegistry.java,v $
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.2  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/