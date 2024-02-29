/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.forecast.SaldoLimit;
import de.willuhn.jameica.hbci.gui.dialogs.KontoLimitsDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Konfiguriert die Kontolimits.
 */
public class KontoLimitsConfigure implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      Konto k = null;
      
      if (context instanceof Konto)
        k = (Konto) context;
      if (context instanceof SaldoLimit)
        k = ((SaldoLimit)context).getKonto();
      
      final KontoLimitsDialog d = new KontoLimitsDialog(k);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to configure account limits",e);
    }
  }

}


