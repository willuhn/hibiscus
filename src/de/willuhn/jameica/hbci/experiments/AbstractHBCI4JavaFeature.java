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
  
  private final org.kapott.hbci.manager.Feature feature;
  
  /**
   * ct.
   * @param f das HBCI4Java-Feature.
   */
  protected AbstractHBCI4JavaFeature(org.kapott.hbci.manager.Feature f)
  {
    this.feature = f;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.experiments.Feature#getName()
   */
  @Override
  public String getName()
  {
    return this.feature.name();
  }

  /**
   * @see de.willuhn.jameica.hbci.experiments.Feature#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "";
  }

  /**
   * @see de.willuhn.jameica.hbci.experiments.Feature#getDefault()
   */
  @Override
  public boolean getDefault()
  {
    return this.feature.getDefault();
  }

  /**
   * @see de.willuhn.jameica.hbci.experiments.Feature#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled)
  {
    this.feature.setEnabled(enabled);
  }

}


