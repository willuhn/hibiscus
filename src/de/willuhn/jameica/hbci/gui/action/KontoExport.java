/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Exporter fuer Konten.
 */
public class KontoExport extends Export
{
  /**
   * ct.
   */
  public KontoExport()
  {
    super(Konto.class);
  }
}
