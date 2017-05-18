/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import javax.annotation.Resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIVariantPinTan;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanController;

/**
 * View zum Bearbeiten der neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanStep2 extends AbstractHBCIAccountView
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
    
    String bank = control.getBankText();
    if (bank != null && bank.trim().length() > 0)
      c.addHeadline(i18n.tr("Zugangsdaten für {0}",bank));
    else
      c.addHeadline(i18n.tr("Zugangsdaten"));
    c.addText(i18n.tr("Bitte geben Sie Ihre Zugangsdaten ein."),true);
    
    InfoPanel panel = this.variant.getInfo();
    c.addPart(panel);
    
    
    Composite comp = this.getComposite(panel);
    Container cs = new SimpleContainer(comp);
    cs.addText("\n" + i18n.tr("Bitte geben Sie Ihre Benutzer- und Kundenkennung sowie Ihre PIN ein."),true);
    
    Input username = control.getUsername();
    Input customer = control.getCustomer();
    
    cs.addPart(username);
    cs.addPart(customer);
    
    username.getControl().addListener(SWT.KeyUp,control.getStep2Listener());
    customer.getControl().addListener(SWT.KeyUp,control.getStep2Listener());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getStep2Button());
    buttons.paint(comp);
  }
}


