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
 * Vorkonfigurierter Context-Menu-Eintrag für Umsätze "Als ungelesen markieren".
 */
public class UmsatzSetUnreadContextMenuItem extends AbstractUmsatzReadContextMenuItem
{
  /**
   * ct.
   */
  public UmsatzSetUnreadContextMenuItem()
  {
    super(i18n.tr("Als ungelesen markieren"), e -> NeueUmsaetze.setUnread(e),"list-add.png");
  }
}
