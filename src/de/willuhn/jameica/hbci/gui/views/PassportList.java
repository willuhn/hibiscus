/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/PassportList.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/29 11:38:57 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
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

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Bank-Zugänge"));
    final PassportControl control = new PassportControl(this);

    Container c = new SimpleContainer(getParent());
    c.addText(i18n.tr("Klicken Sie auf \"Neuer Bank-Zugang...\", um eine neue PIN/TAN- oder " +
    		              "Kartenleser-Konfiguration anzulegen oder eine Schlüsseldatei zu erstellen bzw. zu importieren."),true);

    final PassportTree tree = control.getPassports();

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button(i18n.tr("Neuer Bank-Zugang..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        // new de.willuhn.jameica.hbci.gui.action.AccountNew().handleAction(null);
        new PassportDetail().handleAction(null);
      }
    },null,false,"seahorse-preferences.png"));
    buttons.paint(getParent());

    tree.paint(getParent());
  }
}
