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

import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;

/**
 * Vorkonfigurierter Context-Menu-Eintrag für Umsätze "Alle als gelesen markieren".
 */
public class UmsatzSetAllReadContextMenuItem extends AbstractUmsatzReadContextMenuItem
{
  /**
   * ct.
   */
  public UmsatzSetAllReadContextMenuItem()
  {
    super(i18n.tr("Alle als gelesen markieren"), e -> 
    {
      askMarkReadOnExit();
      NeueUmsaetze.setAllRead();
    },"ok.png");
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
