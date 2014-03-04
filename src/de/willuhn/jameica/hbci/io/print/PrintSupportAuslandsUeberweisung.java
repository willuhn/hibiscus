/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer Auslandsueberweisungen.
 */
public class PrintSupportAuslandsUeberweisung extends AbstractPrintSupportBaseUeberweisung
{
  private AuslandsUeberweisung u = null;
  
  /**
   * ct.
   * @param u die zu druckende Auslandsueberweisung.
   */
  public PrintSupportAuslandsUeberweisung(AuslandsUeberweisung u)
  {
    super(u);
    this.u = u;
  }

  /**
   * @see de.willuhn.jameica.hbci.io.print.AbstractPrintSupport#getTitle()
   */
  String getTitle() throws ApplicationException
  {
    try
    {
      if (u.isTerminUeberweisung())
        return i18n.tr("SEPA-Terminüberweisung");
    }
    catch (RemoteException re)
    {
      Logger.error("check failed",re);
    }
    
    return i18n.tr("SEPA-Überweisung");
  }
}
