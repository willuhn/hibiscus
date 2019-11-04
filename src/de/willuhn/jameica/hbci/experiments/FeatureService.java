/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.experiments;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Services fuer den Zugriff auf die Features.
 */
@Lifecycle(Type.CONTEXT)
public class FeatureService
{
  private Settings settings = new Settings(FeatureService.class);
  private List<Feature> features = new LinkedList<Feature>();
  
  /**
   * Initialisiert den Service.
   */
  @PostConstruct
  private void init()
  {
    final boolean enabled = this.enabled();

    if (enabled)
      Logger.info("loading experimental features");

    // Auch wenn die Features nicht genutzt werden, muessen sie dennoch geladen werden, damit sie konfiguriert werden koennen
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader().getClassFinder().findImplementors(Feature.class);
      for (Class<Feature> c:found)
      {
        try
        {
          final Feature f = service.get(c);
          this.features.add(f);

          if (enabled)
          {
            // Aktivieren/Deaktivieren - je nach gespeichertem Zustand
            final boolean state = this.isEnabled(f);
            Logger.info("  " + c.getSimpleName() + ": " + state);
            f.setEnabled(state);
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to load feature " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load experimental features",e);
    }
  }
  
  /**
   * Liefert die Liste der experimentellen Features.
   * @return Liste der experimentellen Features.
   */
  public List<Feature> getFeatures()
  {
    return this.features;
  }
  
  /**
   * Liefert den aktuellen Zustand des Features.
   * @param f das Feature.
   * @return der aktuelle Zustand des Features.
   */
  public boolean isEnabled(Feature f)
  {
    // Wenn der Feature-Service inaktiv ist, liefern wir generell die Default-Werte
    if (!this.enabled())
      return f.getDefault();
    
    return this.settings.getBoolean(f.getName(),f.getDefault());
  }
  
  /**
   * Aktiviert/Deaktiviert ein Feature.
   * @param f das Feature.
   * @param enabled true, wenn es aktiviert sein soll.
   */
  public void setEnabled(Feature f, boolean enabled)
  {
    Logger.info("set feature " + f.getName() + ": " + enabled);

    // 1. Feature aktivieren/deaktivieren
    f.setEnabled(enabled);
    
    // 2. In den Einstellungen speichern, damit es beim naechsten Start wiederhergestellt wird.
    this.settings.setAttribute(f.getName(),enabled);
  }
  
  /**
   * Liefert true, wenn die experimentellen Features verfuegbar sind.
   * @return true, wenn die experimentellen Features verfuegbar sind.
   */
  public boolean enabled()
  {
    return this.settings.getBoolean("enabled",false);
  }
  
  /**
   * Legt fest, ob die experimentellen Features verfuegbar sein sollen.
   * @param b true, wenn die experimentellen Features verfuegbar sein sollen.
   */
  public void setEnabled(boolean b)
  {
    Logger.info("feature service enabled: " + b);
    this.settings.setAttribute("enabled",b);
  }
}
