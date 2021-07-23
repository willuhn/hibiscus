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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.HBCITraceDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Speichern der HBCI-Trace-Nachrichten.
 */
public class HBCITrace implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      HBCITraceDialog d = new HBCITraceDialog(HBCITraceDialog.POSITION_CENTER);
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
      Logger.error("error while trying to export HBCI trace",e);
      throw new ApplicationException(i18n.tr("Speichern des HBCI-Protokoll fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}


