/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/CherrySmartboardReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:45:48 $
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
import java.rmi.RemoteException;

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
   * @throws RemoteException
   */
  public CherrySmartboardReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Cherry Smartboard";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
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
  public boolean isSupported() throws RemoteException
  {
    return Application.getPlatform().getOS() == Platform.OS_WINDOWS;
  }
}


/**********************************************************************
 * $Log: CherrySmartboardReader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.2  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.1  2008/09/29 23:05:36  willuhn
 * @C ST2000U in Smartboard umbenannt - der CTAPI-Treiber gilt wohl fuer die ganze Serie
 *
 * Revision 1.3  2008/09/15 22:01:04  willuhn
 * @N Presets aktualisiert
 *
 * Revision 1.2  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 * Revision 1.1  2007/11/22 10:01:20  willuhn
 * @N Cherry ST-2000U
 *
 **********************************************************************/