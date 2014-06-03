/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Exporter fuer Ueberweisungen.
 */
public class UeberweisungExport extends Export
{
  /**
   * ct.
   */
  public UeberweisungExport()
  {
    super(Ueberweisung.class);
  }
}
