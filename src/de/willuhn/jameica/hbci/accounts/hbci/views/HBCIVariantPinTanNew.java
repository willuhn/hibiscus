/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanNewController;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zum Erstellen einer neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang PIN/TAN..."));

    final HBCIVariantPinTanNewController control = new HBCIVariantPinTanNewController(this);

    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Schritt 3: Auswahl des Instituts"));
    c.addText(i18n.tr("Bitte geben Sie die BLZ Ihrers Instituts ein."),true);
    
    c.addInput(control.getBLZ());
    c.addInput(control.getURL());
//    c.addInput(control.getHBCIVersion());
  }

}


