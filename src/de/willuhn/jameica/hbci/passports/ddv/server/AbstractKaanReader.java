/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/AbstractKaanReader.java,v $
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
 * Basis-Implementierung fuer die Default-Einstellungen von Kaan-Readern.
 */
public abstract class AbstractKaanReader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public AbstractKaanReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
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
    return false;
  }

  
}


/**********************************************************************
 * $Log: AbstractKaanReader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.3  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.2  2008/09/29 23:14:27  willuhn
 * @N 64Bit-JNI-Lib fuer Windows
 *
 * Revision 1.1  2008/09/15 21:53:44  willuhn
 * @N Kaan TriB@nk + 64Bit-Support
 *
 * Revision 1.6  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 * Revision 1.5  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.4  2005/08/08 15:07:35  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.3  2005/04/11 17:22:00  web0
 * @C backslashes for windows
 *
 * Revision 1.2  2005/01/15 18:06:35  willuhn
 * @C path to correct CT API driver for windows
 *
 * Revision 1.1  2004/09/16 22:35:39  willuhn
 * @N Kaan Standard Plus
 *
 * Revision 1.2  2004/07/27 23:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/