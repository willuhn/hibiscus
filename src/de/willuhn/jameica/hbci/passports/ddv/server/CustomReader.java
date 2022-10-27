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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;

/**
 * Implementierung fuer die Default-Einstellungen eines
 * selbstkonfigurierten Readers.
 */
public class CustomReader extends AbstractReader
{
  @Override
  public String getName()
  {
    return Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Benutzerdefinierter CTAPI-Leser");
  }

  @Override
  public String getCTAPIDriver()
  {
  	return "";
  }

  @Override
  public boolean isSupported()
  {
    return true;
  }

  @Override
  public boolean useSoftPin()
  {
    return true;
  }

}


/**********************************************************************
 * $Log: CustomReader.java,v $
 * Revision 1.2  2010/09/07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/