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
 * Vorkonfigurierter Context-Menu-Eintrag für Umsätze "Als gelesen markieren".
 */
public class UmsatzSetReadContextMenuItem extends AbstractUmsatzReadContextMenuItem
{
  /**
   * ct.
   */
  public UmsatzSetReadContextMenuItem()
  {
    super(i18n.tr("Als gelesen markieren"), e -> {
      askMarkReadOnExit();
      NeueUmsaetze.setRead(e);
    },"list-remove.png");
  }
}
