/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;

/**
 * Action zum Exportieren von SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungExport extends AbstractSepaSammelTransferExport<SepaSammelUeberweisung>
{
  /**
   * @see de.willuhn.jameica.hbci.gui.action.AbstractSepaSammelTransferExport#getExportClass()
   */
  Class<SepaSammelUeberweisung> getExportClass()
  {
    return SepaSammelUeberweisung.class;
  }
}
