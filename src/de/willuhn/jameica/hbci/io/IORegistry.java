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

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Ueber diese Klasse koennen alle verfuegbaren Export-und Import Formate abgerufen werden.
 */
public class IORegistry
{

  // Liste der Export-Filter
  private static final List<Exporter> exporters;

  // Liste der Importer
  private static final List<Importer> importers;

  static
  {
    Logger.info("looking for installed export filters");
    exporters = load(Exporter.class);
    Logger.info("looking for installed import filters");
    importers = load(Importer.class);
  }
  
  /**
   * Sucht im Classpath nach allen Importern/Exportern.
   * @param type zu ladender Typ.
   * @return Liste der gefundenen Importer/Exporter.
   */
  private static synchronized <T extends IO> List<T> load(Class<? extends IO> type)
  {
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    List<T> l = new ArrayList<T>();
    try
    {
      ClassFinder finder = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder();
      Class<T>[] list = finder.findImplementors(type);
      if (list == null || list.length == 0)
        throw new ClassNotFoundException();

      // Initialisieren
      for (Class<T> c:list)
      {
        try
        {
          T io = service.get(c);
          Logger.info("  " + io.getName() + " - " + c.getName());
          l.add(io);
        }
        catch (Exception e)
        {
          Logger.error("error while loading import/export filter " + c.getName(),e);
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
