/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/ScheduleProviderFactory.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/03/28 22:47:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.schedule;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.MultipleClassLoader;

/**
 * Factory zum bequemen Erzeugen eines Schedule-Providers anhand des konkreten Typs. 
 */
public class ScheduleProviderFactory
{
  /**
   * Liefert den Schedule-Provider fuer den angegebenen Typ.
   * @param type der konkrete Typ.
   * @return der Schedule-Provider oder NULL, wenn er nicht existiert.
   */
  public static synchronized ScheduleProvider getScheduleProvider(Class<? extends HibiscusDBObject> type)
  {
    BeanService service        = Application.getBootLoader().getBootable(BeanService.class);
    MultipleClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
    ClassFinder finder         = loader.getClassFinder();
      
    try
    {
      Class<ScheduleProvider>[] classes = finder.findImplementors(ScheduleProvider.class);
      for (Class<ScheduleProvider> c:classes)
      {
        try
        {
          // Checken, ob der konkrete Typ passt
          if (matches(c,type))
            return service.get(c);
        }
        catch (Exception e)
        {
          Logger.error("unable to check/load schedule provider",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no schedule providers found",e);
    }
    
    return null;
  }
  
  /**
   * Prueft, ob der Typ der Klasse zum angegebenen Typ passt.
   * @param c die zu testende Klasse.
   * @param type der zu pruefende Typ.
   * @return true, wenn der Typ passt.
   */
  private static boolean matches(Class c, Class<? extends HibiscusDBObject> type)
  {
    Class concrete = BeanUtil.getType(c);
    return concrete != null && concrete.equals(type);
  }
}



/**********************************************************************
 * $Log: ScheduleProviderFactory.java,v $
 * Revision 1.2  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/