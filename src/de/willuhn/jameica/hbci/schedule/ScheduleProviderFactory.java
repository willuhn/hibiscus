/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/schedule/ScheduleProviderFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/02/20 17:03:50 $
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
    MultipleClassLoader loader = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader();
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
 * Revision 1.1  2012/02/20 17:03:50  willuhn
 * @N Umstellung auf neues Schedule-Framework, welches generisch geplante und tatsaechliche Termine fuer Auftraege und Umsaetze ermitteln kann und kuenftig auch vom Forecast verwendet wird
 *
 **********************************************************************/