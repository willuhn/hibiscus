/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;

/**
 * Dialog für Decouple Verfahren ohne Eingabe einer TAN.
 */
public class DecoupledTANDialog extends TANDialog
{
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @throws RemoteException
   */
  public DecoupledTANDialog(PinTanConfig config) throws RemoteException
  {
    super(config);
    this.setNeedTAN(false);
  }
}
