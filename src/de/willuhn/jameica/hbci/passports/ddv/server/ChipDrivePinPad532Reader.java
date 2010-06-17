/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/ChipDrivePinPad532Reader.java,v $
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
 * "Chipdrive PinPad 532" von Towitoko/SCM/Chipdrive.
 */
public class ChipDrivePinPad532Reader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public ChipDrivePinPad532Reader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Chipdrive Pinpad / SCM SPR 332";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
  {
    if (Application.getPlatform().getOS() == Platform.OS_WINDOWS)
      return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctpcsc32.dll";
    return "";
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
 * $Log: ChipDrivePinPad532Reader.java,v $
 * Revision 1.1  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.8  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
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
 * Revision 1.4  2005/08/08 15:07:36  willuhn
 * @N added jnilib for mac os
 * @N os autodetection for mac os
 *
 * Revision 1.3  2005/04/11 17:22:00  web0
 * @C backslashes for windows
 *
 * Revision 1.2  2004/07/27 23:51:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/