/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/server/KaanTriBankReader.java,v $
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

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;

/**
 * Implementierung fuer die Default-Einstellungen des
 * Kartenlesers Kaan TriB@nk.
 */
public class KaanTriBankReader extends AbstractKaanReader implements Reader
{

  /**
   * @throws RemoteException
   */
  public KaanTriBankReader() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName() throws RemoteException
  {
    return "Kaan TriB@nk";
  }
}


/**********************************************************************
 * $Log: KaanTriBankReader.java,v $
 * Revision 1.1  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.2  2008/09/15 21:53:44  willuhn
 * @N Kaan TriB@nk + 64Bit-Support
 *
 * Revision 1.1  2008/07/29 08:27:43  willuhn
 * @N Kaan TriB@nk
 * @C Pfadtrenner via File.separator
 *
 **********************************************************************/