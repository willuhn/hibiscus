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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Sammel-Lastschriften an.
 */
public class SammelUeberweisungList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    SammelUeberweisungControl control = new SammelUeberweisungControl(this);
    final de.willuhn.jameica.hbci.gui.parts.SammelUeberweisungList table = control.getListe();
    final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportSammelUeberweisung(table));
    table.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        print.setEnabled(table.getSelection() != null);
      }
    });

    GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Überweisungen"));
    GUI.getView().addPanelButton(print);

    table.paint(getParent());
    print.setEnabled(table.getSelection() != null); // einmal initial ausloesen
  }
}
