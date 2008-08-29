/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypUtil.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/08/29 16:46:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse fuer die Umsatzkategorien.
 */
public class UmsatzTypUtil
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Liefert einen sprechenden Namen fuer den Kategorie-Typ.
   * @param type Typ
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @see UmsatzTyp#TYP_EGAL
   * @return sprechender Name.
   */
  public static String getNameForType(int type)
  {
    
    switch (type)
    {
      case UmsatzTyp.TYP_AUSGABE:
        return i18n.tr("Ausgabe");
      case UmsatzTyp.TYP_EINNAHME:
        return i18n.tr("Einnahme");
    }
    return i18n.tr("egal");
  }
}


/*********************************************************************
 * $Log: UmsatzTypUtil.java,v $
 * Revision 1.1  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 **********************************************************************/