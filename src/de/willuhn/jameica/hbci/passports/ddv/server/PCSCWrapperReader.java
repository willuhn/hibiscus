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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * Implementierung fuer die Default-Einstellungen eines Kartenlesers via pcsc-ctapi-wrapper.
 */
public class PCSCWrapperReader extends AbstractReader
{
  private final static Map<Integer,String[]> LOCATIONS = new HashMap<Integer,String[]>();
  
  static
  {
    LOCATIONS.put(Platform.OS_LINUX,new String[]
                   {
                     "/lib/libpcsc-ctapi-wrapper.so",
                     "/usr/lib/libpcsc-ctapi-wrapper.so",
                     "/lib/libpcsc-ctapi-wrapper.so.0.3",
                     "/usr/lib/libpcsc-ctapi-wrapper.so.0.3"
                   }
                 );
    LOCATIONS.put(Platform.OS_LINUX_64,new String[]
                   {
                     "/lib64/libpcsc-ctapi-wrapper.so",
                     "/usr/lib64/libpcsc-ctapi-wrapper.so",
                     "/lib64/libpcsc-ctapi-wrapper.so.0.3",
                     "/usr/lib64/libpcsc-ctapi-wrapper.so.0.3"
                   }
                 );
    LOCATIONS.put(Platform.OS_MAC,new String[]
                   {
                     "/usr/local/lib/pcsc-ctapi-wrapper.dylib"
                   }
                 );
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("CTAPI: PC/SC-Kartenleser via pcsc-ctapi-wrapper");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver()
  {
    int os = Application.getPlatform().getOS();
    String[] locations = LOCATIONS.get(os);

    if (locations != null)
    {
      // Wir nehmen den ersten, den wir finden
      for (String s:locations)
      {
        File f = new File(s);
        if (f.exists() && f.canRead())
          return s;
      }
    }
    
    return "";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported()
  {
    // Haben wir Locations fuer das OS?
    return LOCATIONS.containsKey(Integer.valueOf(Application.getPlatform().getOS()));
  }
}
