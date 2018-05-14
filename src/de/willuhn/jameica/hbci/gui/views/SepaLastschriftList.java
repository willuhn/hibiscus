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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftImport;
import de.willuhn.jameica.hbci.gui.controller.SepaLastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaLastschriftList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen SEPA-Lastschriften an.
 */
public class SepaLastschriftList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    SepaLastschriftControl control = new SepaLastschriftControl(this);
    
    final de.willuhn.jameica.hbci.gui.parts.SepaLastschriftList table = control.getSepaLastschriftListe();
    final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportSepaLastschriftList(table));
    table.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        print.setEnabled(table.getSelection() != null);
      }
    });

    GUI.getView().setTitle(i18n.tr("Vorhandene SEPA-Lastschriften"));
    GUI.getView().addPanelButton(print);

    ButtonArea buttons = table.getButtons();
    buttons.addButton(i18n.tr("Importieren..."),new SepaLastschriftImport(),null,false,"document-open.png");
    buttons.addButton(i18n.tr("Neue SEPA-Lastschrift"), new de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew(),null,false,"text-x-generic.png");

    table.paint(getParent());
    print.setEnabled(table.getSelection() != null); // einmal initial ausloesen
  }
}
