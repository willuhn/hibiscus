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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer HBCI4Java-Features.
 */
public abstract class AbstractHBCI4JavaFeature implements Feature
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private org.kapott.hbci.manager.Feature feature = null;
  
  /**
   * ct.
   * @param f das HBCI4Java-Feature.
   */
  protected AbstractHBCI4JavaFeature(org.kapott.hbci.manager.Feature f)
  {
    this.feature = f;
  }
  
  @Override
  public String getName()
  {
    return this.feature.name();
  }

  @Override
  public String getDescription()
  {
    return "";
  }

  @Override
  public boolean getDefault()
  {
    return this.feature.getDefault();
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    this.feature.setEnabled(enabled);
  }

}


