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

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.logging.Logger;
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
    try
    {
      if (this.getAuftrag().isTerminUeberweisung())
        return i18n.tr("SEPA-Sammelterminüberweisung");
    }
    catch (RemoteException re)
    {
      Logger.error("check failed",re);
    }

    return i18n.tr("SEPA-Sammelüberweisung");
  }
}
