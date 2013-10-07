/**********************************************************************
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SepaLastschrift;

/**
 * Exporter fuer SEPA-Lastschriften.
 */
public class SepaLastschriftExport extends AbstractObjectExport
{

  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractObjectExport#getExportClass()
   */
  Class getExportClass()
  {
    return SepaLastschrift.class;
  }

}
