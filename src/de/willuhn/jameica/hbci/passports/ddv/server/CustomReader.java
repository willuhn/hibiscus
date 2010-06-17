/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/CustomReader.java,v $
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

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;

/**
 * Implementierung fuer die Default-Einstellungen eines
 * selbstkonfigurierten Readers.
 */
public class CustomReader extends AbstractReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public CustomReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Benutzerdefinierter Leser";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getCTAPIDriver()
   */
  public String getCTAPIDriver() throws RemoteException
  {
  	return "";
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#isSupported()
   */
  public boolean isSupported() throws RemoteException
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException
  {
    return true;
  }

}


/**********************************************************************
 * $Log: CustomReader.java,v $
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.3  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.2  2004/07/27 23:39:29  willuhn
 * @N Reader presets
 *
 * Revision 1.1  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 **********************************************************************/