/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.util.ApplicationException;


/**
 * Druck-Support fuer SEPA-Sammel-Lastschriften.
 */
public class PrintSupportSepaSammelLastschrift extends AbstractPrintSupportSammelTransfer
{
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportSepaSammelLastschrift(Object ctx)
  {
    super(ctx);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Sammellastschrift");
  }
}
