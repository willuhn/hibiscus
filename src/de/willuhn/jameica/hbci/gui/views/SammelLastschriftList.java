/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftList.java,v $
 * $Revision: 1.10 $
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
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftImport;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Sammel-Lastschriften an.
 */
public class SammelLastschriftList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    SammelLastschriftControl control = new SammelLastschriftControl(this);
    final de.willuhn.jameica.hbci.gui.parts.SammelLastschriftList table = control.getListe();

    GUI.getView().setTitle(i18n.tr("Vorhandene Sammel-Lastschriften"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSammelLastschrift(table))
    {
      public boolean isEnabled()
      {
        Object sel = table.getSelection();
        return (sel instanceof SammelTransfer) && super.isEnabled();
      }
    });
		
    table.paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Importieren..."),new SammelLastschriftImport(),null,false,"document-open.png");
  	buttons.addButton(i18n.tr("Neue Sammel-Lastschrift"),new SammelLastschriftNew(),null,true,"text-x-generic.png");
  	buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.10  2011/04/11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.9  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/