/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/PCSCReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/09/06 11:54:25 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv.server;

import org.kapott.hbci.manager.HBCIVersion;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * Implementierung des Kartenleser-Supports fuer javax.smartcardio.
 */
public class PCSCReader implements Reader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("PC/SC-Kartenleser (Kobil, ReinerSCT und andere)");
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
    int os = Application.getPlatform().getOS();
    
    return os == Platform.OS_WINDOWS ||
           os == Platform.OS_WINDOWS_64 ||
           os == Platform.OS_LINUX ||
           os == Platform.OS_LINUX_64 ||
           os == Platform.OS_MAC;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getType()
   */
  public Type getType()
  {
    return Type.DDV_PCSC;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getDefaultHBCIVersion()
   */
  public String getDefaultHBCIVersion()
  {
    return HBCIVersion.HBCI_300.getId();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.getName();
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof Reader))
      return false;
    
    Reader other = (Reader) obj;
    String s1 = this.getClass().getName()  + this.getName();
    String s2 = other.getClass().getName() + other.getName();
    return s1.equals(s2);
  }
}
