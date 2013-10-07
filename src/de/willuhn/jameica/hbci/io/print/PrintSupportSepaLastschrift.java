/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer SEPA-Lastschriften.
 */
public class PrintSupportSepaLastschrift extends AbstractPrintSupportBaseUeberweisung
{
  /**
   * ct.
   * @param u die zu druckende Auslandsueberweisung.
   */
  public PrintSupportSepaLastschrift(SepaLastschrift u)
  {
    super(u);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Lastschrift");
  }
}
