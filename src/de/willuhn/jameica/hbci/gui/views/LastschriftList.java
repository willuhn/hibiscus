/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/LastschriftList.java,v $
 * $Revision: 1.13 $
 * $Date: 2011/04/11 14:36:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.LastschriftImport;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportLastschriftList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Lastschrift an.
 */
public class LastschriftList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    LastschriftControl control = new LastschriftControl(this);

    final de.willuhn.jameica.hbci.gui.parts.LastschriftList table = control.getLastschriftListe();

    GUI.getView().setTitle(i18n.tr("Vorhandene Lastschriften"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportLastschriftList(table))
    {
      public boolean isEnabled()
      {
        return table.getSelection() != null && super.isEnabled();
      }
    });
		
    table.paint(getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Importieren..."),new LastschriftImport(),null,false,"document-open.png");
    buttons.addButton(i18n.tr("Neue Lastschrift"),new LastschriftNew(),null,true,"text-x-generic.png");
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: LastschriftList.java,v $
 * Revision 1.13  2011/04/11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.12  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/