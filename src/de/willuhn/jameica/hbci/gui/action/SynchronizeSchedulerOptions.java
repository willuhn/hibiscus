/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeSchedulerOptionsDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Öffnet die Optionen zum Konfigurieren der automatischen Synchronisierung.
 */
public class SynchronizeSchedulerOptions implements Action
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      final SynchronizeSchedulerOptionsDialog d = new SynchronizeSchedulerOptionsDialog(SynchronizeSchedulerOptionsDialog.POSITION_CENTER);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (ApplicationException ae)
    {
      // hier notwendig, da nächster Catch alles fängt
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to configure synchronize options",e);
      throw new ApplicationException(i18n.tr("Konfiguration fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}


