/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import javax.annotation.Resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.internal.buttons.Back;
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

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang PIN/TAN - Schritt 1 von 3"));

    final HBCIVariantPinTanController control = this.getController(HBCIVariantPinTanController.class);

    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Auswahl der Bank"));
    c.addText(i18n.tr("Bitte w�hlen Sie die Bank aus, zu der Sie einen Zugang einrichten m�chten."),true);
    
    InfoPanel panel = this.variant.getInfo();
    c.addPart(panel);
    
    
    Composite comp = this.getComposite(panel);
    Container cs = new SimpleContainer(comp);
    cs.addText("\n" + i18n.tr("Bitte geben Sie die BLZ, BIC oder den Namen Ihrer Bank ein.\nHibiscus wird anschlie�end versuchen, die Adresse des Bankservers zu ermitteln."),true);
    
    final Input bank    = control.getBank();
    final Input url     = control.getURL();
    cs.addPart(bank);
    cs.addPart(url);
    
    bank.getControl().addListener(SWT.KeyUp,control.getStep1Listener());
    url.getControl().addListener(SWT.KeyUp,control.getStep1Listener());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Back());
    buttons.addButton(control.getStep1Button());
    buttons.paint(comp);
  }
}


