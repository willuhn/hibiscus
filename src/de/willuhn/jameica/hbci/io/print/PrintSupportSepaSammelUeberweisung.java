/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.util.ApplicationException;


/**
 * Druck-Support fuer SEPA-Sammel-Ueberweisungen.
 */
public class PrintSupportSepaSammelUeberweisung extends AbstractPrintSupportSepaSammelTransfer<SepaSammelUeberweisung>
{
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportSepaSammelUeberweisung(Object ctx)
  {
    super(ctx);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Sammelüberweisung");
  }
}
