/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import javax.annotation.Resource;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIVariantPinTan;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanController;

/**
 * View zum Erstellen einer neuen PIN/TAN-Config. Seite 1 des Wizzards.
 */
public class HBCIVariantPinTanStep1 extends AbstractHBCIAccountView
{
  @Resource private HBCIVariantPinTan variant;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang PIN/TAN..."));

    final HBCIVariantPinTanController control = this.getController(HBCIVariantPinTanController.class);

    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Schritt 3: Auswahl der Bank"));
    c.addText(i18n.tr("Bitte wählen Sie die Bank aus, zu der Sie einen Zugang einrichten möchten."),true);
    
    InfoPanel panel = this.variant.getInfo();
    c.addPart(panel);
    
    
    Composite comp = this.getComposite(panel);
    Container cs = new SimpleContainer(comp);
    cs.addText("\n" + i18n.tr("Bitte geben Sie die BLZ, BIC oder den Namen Ihrer Bank ein.\nHibiscus wird anschließend versuchen, die Adresse des Bankservers zu ermitteln."),true);
    cs.addPart(control.getBank());
    cs.addPart(control.getURL());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getStep1Button());
    buttons.paint(comp);
  }
}


