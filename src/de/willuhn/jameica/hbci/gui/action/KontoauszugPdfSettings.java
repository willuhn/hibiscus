/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.gui.dialogs.KontoauszugPdfSettingsDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer die Einstellungen des Kontoauszugsabrufes.
 */
public class KontoauszugPdfSettings implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      KontoauszugPdfSettingsDialog d = new KontoauszugPdfSettingsDialog((Konto) context);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (Exception e)
    {
      Logger.error("error while changing account statement fetch settings",e);
    }
  }

}


