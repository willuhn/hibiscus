/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/keyformat/RDH2Format.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:26:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#getName()
   */
  public String getName()
  {
    return i18n.tr("RDH-Format (StarMoney, ProfiCash, VR-NetWorld, Sfirm)");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#hasFeature(int)
   */
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


  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.HBCI4JavaFormat#getPassportType()
   */
  String getPassportType()
  {
    return "RDHXFile";
  }
}
