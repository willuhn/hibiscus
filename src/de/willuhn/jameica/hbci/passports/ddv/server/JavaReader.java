/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/Attic/JavaReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/09/01 12:16:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv.server;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung des Kartenleser-Supports fuer javax.smartcardio.
 */
public class JavaReader implements Reader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("PC/SC-Kartenleser");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getPort()
   */
  public String getPort()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTNumber()
   */
  public int getCTNumber()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isJavaReader()
   */
  public boolean isJavaReader()
  {
    return true;
  }

}



/**********************************************************************
 * $Log: JavaReader.java,v $
 * Revision 1.1  2011/09/01 12:16:08  willuhn
 * @N Kartenleser-Suche kann jetzt abgebrochen werden
 * @N Erster Code fuer javax.smartcardio basierend auf dem OCF-Code aus HBCI4Java 2.5.8
 *
 **********************************************************************/