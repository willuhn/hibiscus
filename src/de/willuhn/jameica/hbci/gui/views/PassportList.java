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
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.controller.PassportControl;
import de.willuhn.jameica.hbci.gui.parts.PassportTree;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zum Anzeigen der Bank-Zugaenge.
 */
public class PassportList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Bank-Zug�nge"));
    final PassportControl control = new PassportControl(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Klicken Sie auf \"Neuer Bank-Zugang...\", um eine neue PIN/TAN- oder " +
    		              "Kartenleser-Konfiguration anzulegen oder eine Schl�sseldatei zu erstellen bzw. zu importieren."),true);

    final PassportTree tree = control.getPassports();

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button(i18n.tr("Neuer Bank-Zugang..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        // new de.willuhn.jameica.hbci.gui.action.AccountNew().handleAction(null);
        new PassportDetail().handleAction(null);
      }
    },null,false,"list-add.png"));
    buttons.paint(getParent());

    tree.paint(getParent());
  }
}
