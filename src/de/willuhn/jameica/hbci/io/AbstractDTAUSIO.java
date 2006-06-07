/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/AbstractDTAUSIO.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/06/07 22:42:00 $
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
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse fuer DTAUS-Import/Export.
 */
public abstract class AbstractDTAUSIO implements IO
{
  I18N i18n = null;

  /**
   * ct.
   */
  public AbstractDTAUSIO()
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("DTAUS-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Kein Typ angegeben?
    if (objectType == null)
      return null;
    
    // Wir unterstuetzen erstmal nur Ueberweisungen und Lastschriften
    if (!objectType.equals(Ueberweisung.class) && !objectType.equals(Lastschrift.class))
      return null;

    return new IOFormat[] { new MyIOFormat(objectType) };
  }

  /**
   * Hilfsklasse, damit wir uns den Objekt-Typ merken koennen.
   * @author willuhn
   */
  class MyIOFormat implements IOFormat
  {
    Class type = null;
    
    /**
     * ct.
     * @param type
     */
    private MyIOFormat(Class type)
    {
      this.type = type;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      return i18n.tr("DTAUS-Format");
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
     */
    public String[] getFileExtensions()
    {
      return new String[] {"*.dta"};
    }
  }
}


/*********************************************************************
 * $Log: AbstractDTAUSIO.java,v $
 * Revision 1.1  2006/06/07 22:42:00  willuhn
 * @N DTAUSExporter
 * @N Abstrakte Basis-Klasse fuer Export und Import
 *
 **********************************************************************/