/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/OmniKey4000Reader.java,v $
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
 * Kartenlesers OmniKey 4000 PCMCIA.
 * Danke an Martin Clausen fuer die Hinweise.
 */
public class OmniKey4000Reader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public OmniKey4000Reader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Omnikey 4000 (PC-Card)";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
  {
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_WINDOWS:
        return getCTAPIDriverPath().getAbsolutePath() + File.separator + "ctdeutin.dll";
      
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

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.server.AbstractReader#getCTNumber()
   */
  public int getCTNumber() throws RemoteException
  {
    return 1;
  }

}


/**********************************************************************
 * $Log: OmniKey4000Reader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.7  2009/09/30 13:03:58  willuhn
 * @B typo
 *
 * Revision 1.6  2008/11/17 23:22:38  willuhn
 * @C Code zur Ermittlung des OS in Jameica verschoben
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
 * Revision 1.2  2006/08/03 22:18:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 **********************************************************************/