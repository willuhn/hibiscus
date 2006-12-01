/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/AbstractHibiscusIO.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/12/01 01:28:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Basisklasse fuer Import/Export im Hibiscus-Format.
 */
public abstract class AbstractHibiscusIO implements IO
{
  I18N i18n = null;
  
  /**
   * ct. 
   */
  public AbstractHibiscusIO()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    return new IOFormat[]{new IOFormat() {
    
      public String getName()
      {
        return i18n.tr("Hibiscus-Format (experimentell!)");
      }
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"hib"};
      }
    
    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Hibiscus-Format (experimentell!)");
  }

}


/*********************************************************************
 * $Log: AbstractHibiscusIO.java,v $
 * Revision 1.1  2006/12/01 01:28:16  willuhn
 * @N Experimenteller Import-Export-Code
 *
 **********************************************************************/