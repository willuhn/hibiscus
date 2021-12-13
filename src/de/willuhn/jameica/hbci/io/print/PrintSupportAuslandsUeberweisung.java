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

import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer Auslandsueberweisungen.
 */
public class PrintSupportAuslandsUeberweisung extends AbstractPrintSupportSepaTransfer<AuslandsUeberweisung>
{
  /**
   * ct.
   * @param u die zu druckende Auslandsueberweisung.
   */
  public PrintSupportAuslandsUeberweisung(AuslandsUeberweisung u)
  {
    super(u);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    try
    {
      if (this.getTransfer().isTerminUeberweisung())
        return i18n.tr("SEPA-Termin�berweisung");
      else if (this.getTransfer().isUmbuchung())
        return i18n.tr("SEPA-Umbuchung");
    }
    catch (RemoteException re)
    {
      Logger.error("check failed",re);
    }
    
    return i18n.tr("SEPA-�berweisung");
  }
}
