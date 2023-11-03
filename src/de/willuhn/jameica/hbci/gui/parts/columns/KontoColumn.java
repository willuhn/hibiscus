/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts.columns;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorformatierte Konto-Spalte.
 */
public class KontoColumn extends Column
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   */
  public KontoColumn()
  {
    super("konto_id", i18n.tr("Konto"),new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null || !(o instanceof Konto))
          return null;
        Konto k = (Konto) o;
        try
        {
          return k.getLongName();
        }
        catch (RemoteException r)
        {
          Logger.error("unable to display konto",r);
          return null;
        }
      }
    },false,Column.ALIGN_AUTO,Column.SORT_BY_DISPLAY); // Sortierung nach Anzeige, nicht nur nach Kontonummer
  }
}
