/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/columns/BlzColumn.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/17 00:00:02 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts.columns;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorformatierte Blz-Spalte.
 */
public class BlzColumn extends Column
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param id Name der Spalte.
   */
  public BlzColumn(String id)
  {
    super(id,i18n.tr("Gegenkonto BLZ"),new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIUtils.getNameForBLZ(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    });
  }
}


/**********************************************************************
 * $Log: BlzColumn.java,v $
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/
