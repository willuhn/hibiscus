/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;

/**
 * Action zum Exportieren von SEPA-Sammellastschriften.
 */
public class SepaSammelLastschriftExport extends AbstractSepaSammelTransferExport<SepaSammelLastschrift>
{
  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSepaSammelTransferExport#getExportClass()
   */
  Class<SepaSammelLastschrift> getExportClass()
  {
    return SepaSammelLastschrift.class;
  }
}
