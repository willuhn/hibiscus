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
 * Kartenlesers OmniKey 4000 PCMCIA.
 * Danke an Martin Clausen fuer die Hinweise.
 */
public class OmniKey4000Reader extends AbstractReader
{
  @Override
  public String getName()
  {
    return "CTAPI: Omnikey 4000 (PC-Card)";
  }

  @Override
  public String getCTAPIDriver()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctdeutin.dll";
      
      default:
        return "";
    }
  }

  @Override
  public boolean isSupported()
  {
    return Application.getPlatform().getOS() == Platform.OS_WINDOWS;
  }

  @Override
  public boolean useSoftPin()
  {
    return true;
  }

  @Override
  public int getCTNumber()
  {
    return 1;
  }

}


/**********************************************************************
 * $Log: OmniKey4000Reader.java,v $
 * Revision 1.2  2010/09/07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/