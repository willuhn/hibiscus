/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelUeberweisungList.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/04/11 16:48:33 $
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
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
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

    GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Überweisungen"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSammelUeberweisung(table))
    {
      public boolean isEnabled()
      {
        Object sel = table.getSelection();
        return (sel instanceof SammelTransfer) && super.isEnabled();
      }
    });
		
    table.paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Importieren..."),new SammelUeberweisungImport(),null,false,"document-open.png");
    buttons.addButton(i18n.tr("Neue Sammel-Überweisung"),new SammelUeberweisungNew(),null,true,"text-x-generic.png");
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: SammelUeberweisungList.java,v $
 * Revision 1.7  2011/04/11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.6  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/