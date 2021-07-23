/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;


/**
 * Implementierung des Schluesselformats fuer RDH2.
 * http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=50285
 */
public class RDH2Format extends HBCI4JavaFormat
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public String getName()
  {
    return i18n.tr("RDH-Format (StarMoney, ProfiCash, VR-NetWorld, Sfirm)");
  }

  @Override
  public boolean hasFeature(int feature)
  {
    switch (feature)
    {
      case KeyFormat.FEATURE_CREATE:
        return false; // Erstellung von Dateien mit dem Format unterstuetzen wir nicht mehr.
      case KeyFormat.FEATURE_IMPORT:
        return true;
    }
    Logger.warn("unknown feature " + feature);
    return false;
  }


  @Override
  String getPassportType()
  {
    return "RDHXFile";
  }
}
