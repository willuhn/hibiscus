/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/CherrySmartboardReader.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/09/07 15:28:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;

import java.io.File;

import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Platform;

/**
 * Implementierung fuer die Default-Einstellungen der
 * Kartenleser-Reihe Cherry Smartboard.
 */
public class CherrySmartboardReader extends AbstractReader implements Reader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return "CTAPI: Cherry Smartboard";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver()
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "CTChyCTApiSp.dll";
      
      default:
        return "";
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported()
  {
    return Application.getPlatform().getOS() == Platform.OS_WINDOWS;
  }
}


/**********************************************************************
 * $Log: CherrySmartboardReader.java,v $
 * Revision 1.2  2010/09/07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/