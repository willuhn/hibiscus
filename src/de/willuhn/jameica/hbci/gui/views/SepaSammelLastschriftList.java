/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftImport;
import de.willuhn.jameica.hbci.gui.controller.SepaSammelLastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen SEPA-Sammel-Lastschriften an.
 */
public class SepaSammelLastschriftList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    SepaSammelLastschriftControl control = new SepaSammelLastschriftControl(this);
    
    final de.willuhn.jameica.hbci.gui.parts.SepaSammelLastschriftList table = control.getListe();
    // final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportSepaSammelLastschrift(table));
//    table.addSelectionListener(new Listener() {
//      public void handleEvent(Event event)
//      {
//        print.setEnabled(table.getSelection() != null);
//      }
//    });
    
    GUI.getView().setTitle(i18n.tr("Vorhandene SEPA-Sammellastschriften"));
//    GUI.getView().addPanelButton(print);

    ButtonArea buttons = table.getButtons();
    buttons.addButton(i18n.tr("Importieren..."),new SepaSammelLastschriftImport(),null,false,"document-open.png");
    buttons.addButton(i18n.tr("Neue SEPA-Sammellastschrift"),new de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftNew(),null,false,"text-x-generic.png");

    table.paint(getParent());
//    print.setEnabled(table.getSelection() != null); // einmal initial ausloesen
  }
}
