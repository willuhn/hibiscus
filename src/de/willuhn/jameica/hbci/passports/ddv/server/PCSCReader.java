/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("PC/SC-Kartenleser (Kobil, ReinerSCT und andere)");
  }

  @Override
  public String getCTAPIDriver()
  {
    return null;
  }

  @Override
  public String getPort()
  {
    return null;
  }

  @Override
  public int getCTNumber()
  {
    return 0;
  }

  @Override
  public boolean isSupported()
  {
    int os = Application.getPlatform().getOS();
    
    return os == Platform.OS_WINDOWS ||
           os == Platform.OS_WINDOWS_64 ||
           os == Platform.OS_LINUX ||
           os == Platform.OS_LINUX_64 ||
           os == Platform.OS_MAC;
  }

  @Override
  public boolean useSoftPin()
  {
    return false;
  }

  @Override
  public Type getType()
  {
    return Type.DDV_PCSC;
  }
  
  @Override
  public String getDefaultHBCIVersion()
  {
    return HBCIVersion.HBCI_300.getId();
  }

  @Override
  public String toString()
  {
    return this.getName();
  }
  
  @Override
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
