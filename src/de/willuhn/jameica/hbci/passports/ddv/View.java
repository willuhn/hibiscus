/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Liste der Kartenleser-Konfigurationen an.
 */
public class View extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Chipkarten-Konfigurationen"));

    final Controller control = new Controller(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Klicken Sie bitte auf \"Neue Kartenleser-Konfiguration anlegen\" um einen neuen Kartenleser-basierten Bankzugang anzulegen."),true);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Neue Kartenleser-Konfiguration anlegen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleCreate();
      }
    },null,false,"document-new.png");
    buttons.paint(getParent());
    
    control.getConfigList().paint(getParent());
    
    
  }
}
