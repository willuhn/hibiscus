/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIAccountNewController;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zum Erstellen eines neuen HBCI-Bankzugangs.
 */
public class HBCIAccountNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang..."));
    HBCIAccountNewController control = new HBCIAccountNewController(this);
    
    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Schritt 2: Auswahl des Verfahrens"));
    c.addText(i18n.tr("Bitte wählen Sie die Art des FinTS/HBCI-Verfahrens."),true);
    
    for (InfoPanel panels:control.getVariants())
    {
      panels.paint(this.getParent());
    }
  }

}


