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

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * Basis-Implementierung fuer die Default-Einstellungen von Kaan-Readern.
 */
public abstract class AbstractKaanReader extends AbstractReader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        // Wir schauen mal, ob der CT-Treiber im System installiert ist
        File f32 = new File("/usr/lib/libct.so");
        if (f32.exists())
          return f32.getAbsolutePath();
        
        // Ne, dann nehmen wir den mitgelieferten
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "libct.so";
      case Platform.OS_LINUX_64:
        // Wir schauen mal, ob der CT-Treiber im System installiert ist
        File f64 = new File("/usr/lib64/libct.so");
        if (f64.exists())
          return f64.getAbsolutePath();
        return "";

      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "CT32.dll";
      
      default:
        return "";
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported()
  {
    int os = Application.getPlatform().getOS();
    return os == Platform.OS_LINUX || 
           os == Platform.OS_WINDOWS || 
           os == Platform.OS_LINUX_64;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin()
  {
    return false;
  }

  
}


/**********************************************************************
 * $Log: AbstractKaanReader.java,v $
 * Revision 1.2  2010/09/07 15:28:04  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/