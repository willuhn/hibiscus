/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIAccountNewController;

/**
 * View zum Erstellen eines neuen HBCI-Bankzugangs.
 */
public class HBCIAccountNew extends AbstractHBCIAccountView
{
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
    
    for (InfoPanel panel:control.getVariants())
    {
      c.addPart(panel);
    }
  }

}


