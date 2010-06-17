/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/ReinerSCTCyberJackReader.java,v $
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
 * Implementierung fuer die Default-Einstellungen des
 * "CyberJack PinPad" von ReinerSCT.
 */
public class ReinerSCTCyberJackReader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public ReinerSCTCyberJackReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "ReinerSCT cyberjack";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        return "/usr/lib/libctapi-cyberjack.so";
      
      case Platform.OS_LINUX_64:
        return "/usr/lib64/libctapi-cyberjack.so";

      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctrsct32.dll";
      
      default:
        return "";
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported() throws RemoteException
  {
    int os = Application.getPlatform().getOS();
    return os == Platform.OS_LINUX || 
           os == Platform.OS_WINDOWS || 
           os == Platform.OS_LINUX_64;
  }
}


/**********************************************************************
 * $Log: ReinerSCTCyberJackReader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.7  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.6  2008/09/29 23:14:27  willuhn
 * @N 64Bit-JNI-Lib fuer Windows
 *
 * Revision 1.5  2008/09/15 22:01:04  willuhn
 * @N Presets aktualisiert
 *
 * Revision 1.4  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 * Revision 1.3  2007/07/24 13:50:27  willuhn
 * @N BUGZILLA 61
 *
 * Revision 1.2  2007/03/22 12:49:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/03/22 12:44:48  willuhn
 * @N Treiber-Preset fuer ReinerSCT CyberJack
 *
 **********************************************************************/