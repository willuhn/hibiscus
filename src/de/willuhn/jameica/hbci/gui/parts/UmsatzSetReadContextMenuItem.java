/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierter Context-Menu-Eintrag für Umsätze "Als gelesen markieren".
 */
public class UmsatzSetReadContextMenuItem extends CheckedContextMenuItem
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   */
  public UmsatzSetReadContextMenuItem()
  {
    super(i18n.tr("Als gelesen markieren"), e -> NeueUmsaetze.setRead(e),"list-remove.png");
  }
}
