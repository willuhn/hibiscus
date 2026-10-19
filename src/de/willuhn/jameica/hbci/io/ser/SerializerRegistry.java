/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.ser;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;

/**
 * Liefert die Liste der verfügbaren Serializer.
 */
public class SerializerRegistry
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Map<String,Class<Serializer>> serializers = new HashMap<>();
  
  @Resource private BeanService service;
  
  /**
   * Wir laden die Serializer on-demand.
   */
  @PostConstruct
  private void init()
  {
    Logger.info("init serializer registry");
    try
    {
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class<Serializer>[] list = finder.findImplementors(Serializer.class);
      if (list == null || list.length == 0)
      {
        Logger.warn("suspect - no serializers found");
        return;
      }

      // Initialisieren
      for (Class<Serializer> c:list)
      {
        try
        {
          final Serializer s = this.service.get(c);
          final String id = s.getIdentifier();
          Logger.info("  " + id);
          
          // Wir speichern nicht die Instanzen sondern nur die Klassen.
          this.serializers.put(id,c);
        }
        catch (Exception e)
        {
          Logger.error("error while loading serializer " + c.getName(),e);
        }
      }

    }
    catch (Exception e)
    {
      Logger.error("error while loading serializers",e);
    }
  }
  
  /**
   * Liefert den Serializer mit der angegebenen ID.
   * @param id die ID.
   * @return der Serializer.
   * @throws ApplicationException
   */
  public Serializer getSerializer(String id) throws ApplicationException
  {
    final Class<Serializer> s = this.serializers.get(id);
    if (s == null)
      throw new ApplicationException(i18n.tr("Das Datenformat {0} ist unbekannt",id));
    
    return this.service.get(s);
  }

}
