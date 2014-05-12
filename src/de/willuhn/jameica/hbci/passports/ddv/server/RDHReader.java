/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv.server;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung des Kartenleser-Supports RDH-Karten via javax.smartcardio.
 */
public class RDHReader extends PCSCReader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("RDH-Karte via PC/SC-Kartenleser");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getType()
   */
  public Type getType()
  {
    return Type.RDH_PCSC;
  }
}
