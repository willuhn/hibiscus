/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer eine einzelne Buchung in einer SEPA-Sammelueberweisung.
 */
public class PrintSupportSepaSammelUeberweisungBuchung extends AbstractPrintSupportSepaSammelTransferBuchung<SepaSammelUeberweisungBuchung>
{
  /**
   * ct.
   * @param u der zu druckende Auftrag.
   */
  public PrintSupportSepaSammelUeberweisungBuchung(SepaSammelUeberweisungBuchung u)
  {
    super(u);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Buchung einer SEPA-Sammelüberweisung");
  }
}
