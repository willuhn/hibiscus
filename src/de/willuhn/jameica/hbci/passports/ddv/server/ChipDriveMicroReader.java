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
 * "Chipdrive Micro" von Towitoko/SCM/Chipdrive.
 */
public class ChipDriveMicroReader extends AbstractReader
{
  @Override
  public String getName()
  {
    return "CTAPI: Chipdrive Micro / Towitoko Kartenzwerg";
  }

  @Override
  public String getCTAPIDriver()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "libtowitoko-2.0.7.so";
      
      case Platform.OS_LINUX_64:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "libtowitoko-2.0.7-amd64.so";

      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "cttwkw32.dll";
      
      default:
        return "";
    }
  }

  @Override
  public boolean isSupported()
  {
    int os = Application.getPlatform().getOS();
    return os == Platform.OS_LINUX || 
           os == Platform.OS_WINDOWS || 
           os == Platform.OS_LINUX_64;
  }

  @Override
  public boolean useSoftPin()
  {
    return true;
  }
  
  @Override
  public String getPort()
  {
    return "COM/USB";
  }
}


/**********************************************************************
 * $Log: ChipDriveMicroReader.java,v $
 * Revision 1.2  2010/09/07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/