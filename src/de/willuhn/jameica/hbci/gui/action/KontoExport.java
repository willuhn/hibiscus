/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
