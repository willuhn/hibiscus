/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/columns/AusgefuehrtColumn.java,v $
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

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorformatierte Spalte fuer Status "ausgefuehrt".
 */
public class AusgefuehrtColumn extends Column
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param id Name der Spalte.
   */
  public AusgefuehrtColumn(String id)
  {
    super(id,i18n.tr("Status"),new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
  }
}


/**********************************************************************
 * $Log: AusgefuehrtColumn.java,v $
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/
