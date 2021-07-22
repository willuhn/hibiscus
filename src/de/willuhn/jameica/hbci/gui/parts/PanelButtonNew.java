/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Vorkonfigurierter Button fuer die Erstellung eines neuen Auftrages.
 */
public class PanelButtonNew extends PanelButton
{
  /**
   * ct.
   * @param type der Typ es Auftrages.
   */
  public PanelButtonNew(final Class type)
  {
    super("list-add.png", new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        new Open().handleAction(type);
      }
    }, Application.getI18n().tr("Neuen Auftrag anlegen"));
  }
}
