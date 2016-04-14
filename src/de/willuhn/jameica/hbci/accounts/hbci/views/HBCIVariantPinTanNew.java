/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import javax.annotation.Resource;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIVariantPinTan;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanNewController;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zum Erstellen einer neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Resource private HBCIVariantPinTan variant;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang PIN/TAN..."));

    final HBCIVariantPinTanNewController control = new HBCIVariantPinTanNewController(this);

    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Schritt 3: Auswahl der"));
    c.addText(i18n.tr("Bitte wählen Sie die Bank, zu der Sie einen Zugang einrichten möchten."),true);
    c.addPart(this.variant.getInfo());
    
    c.addText("\n" + i18n.tr("Bitte geben Sie die BLZ Ihrer Bank ein.\nHibiscus wird anschließend versuchen, die Adresse des Bankservers zu ermitteln."),true);
    c.addPart(control.getBLZ());
    c.addPart(control.getURL());
//    c.addInput(control.getHBCIVersion());
  }

}


