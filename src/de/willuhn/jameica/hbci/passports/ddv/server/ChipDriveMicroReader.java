/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/ChipDriveMicroReader.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:45:49 $
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
 * "Chipdrive Micro" von Towitoko/SCM/Chipdrive.
 */
public class ChipDriveMicroReader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public ChipDriveMicroReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Chipdrive Micro / Towitoko Kartenzwerg";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
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

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException
  {
    return true;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.server.AbstractReader#getPort()
   */
  public String getPort() throws RemoteException
  {
    return "COM/USB";
  }
}


/**********************************************************************
 * $Log: ChipDriveMicroReader.java,v $
 * Revision 1.1  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.9  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.8  2008/09/29 23:14:27  willuhn
 * @N 64Bit-JNI-Lib fuer Windows
 *
 * Revision 1.7  2008/09/15 22:01:04  willuhn
 * @N Presets aktualisiert
 *
 * Revision 1.6  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 * Revision 1.5  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.4  2006/01/08 22:22:37  willuhn
 * @B 176
 * @B 177
 *
 * Revision 1.3  2005/08/08 15:07:35  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.2  2004/07/27 23:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/