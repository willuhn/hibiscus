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
 * Implementierung fuer die Default-Einstellungen des
 * "CyberJack PinPad" von ReinerSCT.
 */
public class ReinerSCTCyberJackReader extends AbstractReader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return "CTAPI: ReinerSCT cyberjack";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        return "/usr/lib/libctapi-cyberjack.so";

      case Platform.OS_LINUX_64:
        return "/usr/lib64/libctapi-cyberjack.so";

      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctrsct32.dll";

      case Platform.OS_WINDOWS_64:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctrsct64.dll";

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
           os == Platform.OS_WINDOWS_64 ||
           os == Platform.OS_LINUX_64;
  }
}

/**********************************************************************
 * $Log: ReinerSCTCyberJackReader.java,v $
 * Revision 1.4  2012/05/18 13:08:57  willuhn
 * @B BUGZILLA 1236
 *
 * Revision 1.3  2010-09-07 15:28:04  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.2  2010-07-25 23:56:09  willuhn
 * @N Suchpfad fuer Windows 64Bit-CTAPI-Treiber (siehe Mail von Tobias vom 26.07.2010)
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/