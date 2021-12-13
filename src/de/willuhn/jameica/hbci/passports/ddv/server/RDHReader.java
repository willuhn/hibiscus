/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv.server;

import org.kapott.hbci.manager.HBCIVersion;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung des Kartenleser-Supports RDH-Karten via javax.smartcardio.
 */
public class RDHReader extends PCSCReader
{
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("RDH-Karte via PC/SC-Kartenleser");
  }
  
  @Override
  public Type getType()
  {
    return Type.RDH_PCSC;
  }
  
  @Override
  public String getDefaultHBCIVersion()
  {
    return HBCIVersion.HBCI_300.getId();
  }

}
