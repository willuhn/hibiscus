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

import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierter Context-Menu-Eintrag für Umsätze "Alle als gelesen markieren".
 */
public class UmsatzSetAllReadContextMenuItem extends ContextMenuItem
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   */
  public UmsatzSetAllReadContextMenuItem()
  {
    super(i18n.tr("Alle als gelesen markieren"), e -> NeueUmsaetze.reset(),"ok.png");
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
   */
  @Override
  public boolean isEnabledFor(Object o)
  {
    return NeueUmsaetze.size() > 0;
  }
}
