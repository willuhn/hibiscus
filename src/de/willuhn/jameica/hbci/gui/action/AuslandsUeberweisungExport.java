/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;

/**
 * Exporter fuer Auslandsueberweisungen.
 */
public class AuslandsUeberweisungExport extends Export
{
  /**
   * ct.
   */
  public AuslandsUeberweisungExport()
  {
    super(AuslandsUeberweisung.class);
  }
}
