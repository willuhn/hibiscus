/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoauszugMoveDialog;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Verschieben von Kontoauszuegen.
 */
public class KontoauszugMove implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Kontoauszug[] list = null;
    if (context instanceof Kontoauszug)
      list = new Kontoauszug[]{(Kontoauszug)context};
    else if (context instanceof Kontoauszug[])
      list = (Kontoauszug[]) context;

    try
    {
      KontoauszugMoveDialog d = new KontoauszugMoveDialog(list);
      d.open();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Logger.debug("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("unable to move files",e);
      throw new ApplicationException(i18n.tr("Verschieben fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}


