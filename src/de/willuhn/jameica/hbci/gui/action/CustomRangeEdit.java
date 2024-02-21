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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CustomRangeDialog;
import de.willuhn.jameica.hbci.server.Range.Category;
import de.willuhn.jameica.hbci.server.Range.CustomRange;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Bearbeiten eines benutzerspezifischen Zeitraumes.
 */
public class CustomRangeEdit implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Category cat = null;
  
  /**
   * ct.
   * @param cat die Kategorie.
   */
  public CustomRangeEdit(Category cat)
  {
    this.cat = cat;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof CustomRange))
      context = CustomRange.create();

    try
    {
      final CustomRangeDialog d = new CustomRangeDialog(this.cat,(CustomRange)context);
      d.open();
      GUI.getCurrentView().reload();
    }
    catch (OperationCanceledException|ApplicationException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to edit custom range",e2);
      throw new ApplicationException(i18n.tr("Bearbeiten des Zeitraums fehlgeschlagen"));
    }
  }

}


