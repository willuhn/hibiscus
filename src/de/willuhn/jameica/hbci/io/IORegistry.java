/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/IORegistry.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/17 00:22:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.util.ArrayList;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Ueber diese Klasse koennen alle verfuegbaren Export-und Import Formate abgerufen werden.
 */
public class IORegistry
{

  // Liste der Export-Filter
  private static ArrayList exporters = null;

  // Liste der Importer
  private static ArrayList importers = null;

  static
  {
    Logger.info("looking for installed import/export filters");
    exporters = load(Exporter.class);
    importers = load(Importer.class);
  }
  
  /**
   * Sucht im Classpath nach allen Importern/Exportern.
   * @param type zu ladender Typ.
   * @return Liste der gefundenen Importer/Exporter.
   */
  private static synchronized ArrayList load(Class type)
  {
    ArrayList l = new ArrayList();
    try
    {
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class[] list = finder.findImplementors(type);
      if (list == null || list.length == 0)
        throw new ClassNotFoundException();

      // Initialisieren
      for (int i=0;i<list.length;++i)
      {
        try
        {
          Logger.info("trying to load " + list[i].getName());
          IO io = (IO) list[i].newInstance();
          Logger.info("loaded: " + io.getName());
          l.add(io);
        }
        catch (Exception e)
        {
          Logger.error("error while loading import/export filter " + list[i].getName(),e);
        }
      }

    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no filters found for type: " + type.getName());
    }
    return l;
  }

  /**
   * Liefert eine Liste aller verfuegbaren Export-Formate.
   * @return Export-Filter.
   */
  public static Exporter[] getExporters()
  {
    return (Exporter[]) exporters.toArray(new Exporter[exporters.size()]);
  }

  /**
   * Liefert eine Liste aller verfuegbaren Import-Formate.
   * @return Import-Filter.
   */
  public static Importer[] getImporters()
  {
    return (Importer[]) importers.toArray(new Importer[importers.size()]);
  }
  
}


/**********************************************************************
 * $Log: IORegistry.java,v $
 * Revision 1.2  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
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