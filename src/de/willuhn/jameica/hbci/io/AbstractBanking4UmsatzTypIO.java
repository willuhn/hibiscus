/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basisklasse für Importer und Exporter fuer Umsatzkategorien im Banking4-Format.
 */
public abstract class AbstractBanking4UmsatzTypIO implements IO
{
  protected final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  protected final static String SEP = ":";

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("Banking4-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!UmsatzTyp.class.equals(objectType))
      return null;
    
    IOFormat f = new IOFormat() {
      @Override
      public String getName()
      {
        return AbstractBanking4UmsatzTypIO.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] {"*.tre"};
      }
    };
    return new IOFormat[] { f };
  }
}
