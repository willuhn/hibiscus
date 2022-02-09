/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.AccountNewController;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View mit dem ersten Schritt bei der Erstellung eines neuen Accounts.
 */
public class AccountNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer Bankzugang..."));
    AccountNewController control = new AccountNewController(this);
    
    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Art des Bankzugangs"));
    c.addText(i18n.tr("Bitte w�hlen Sie die Art des anzulegenden Bankzugangs."),true);
    
    for (InfoPanel panel:control.getAccountProviders())
    {
      c.addPart(panel);
    }
  }

}


