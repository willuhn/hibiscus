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
        return true;
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


/**********************************************************************
 * $Log: RDH2Format.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.8  2009/11/02 23:05:55  willuhn
 * @B RDHX-Schluessel wurden als RDHNew angelegt - siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=62310#62310
 *
 * Revision 1.7  2009/10/20 23:36:21  willuhn
 * @C RDH2File in RDHXFile umbenannt
 *
 * Revision 1.6  2008/08/29 17:06:25  willuhn
 * @N InvalidPassphraseException beruecksichtigen
 *
 * Revision 1.5  2008/07/28 17:36:12  willuhn
 * @N RDH2-Format als importierbar markiert
 *
 * Revision 1.4  2008/07/25 12:57:06  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.1  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.1  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 **********************************************************************/
