/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.NachrichtCopy;
import de.willuhn.jameica.hbci.gui.controller.NachrichtControl;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine System-Nachricht an.
 */
public class NachrichtDetails extends AbstractView {

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

    NachrichtControl control = new NachrichtControl(this);

    Nachricht n = control.getNachricht();

		GUI.getView().setTitle(i18n.tr("System-Nachricht vom {0}", HBCI.DATEFORMAT.format(n.getDatum())));

    SimpleContainer container = new SimpleContainer(getParent(),true,1);
    String name = HBCIProperties.getNameForBank(n.getBLZ());
    if (name != null)
      container.addText(i18n.tr("{0} [BLZ: {1}]", new String[] {name,n.getBLZ()}) + "\n",true);
    else
      container.addText(i18n.tr("BLZ: {0}", new String[] {n.getBLZ()}) + "\n",true);

    String msg = n.getNachricht();
    msg = msg.replace("\\n","\n"); // Falls die Zeilenumbrueche escaped waren

    TextAreaInput text = new TextAreaInput(msg);
    text.setEnabled(false);
    text.paint(container.getComposite());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("In Zwischenablage kopieren"),new NachrichtCopy(),n,false,"edit-copy.png");
    buttons.addButton(i18n.tr("Löschen"),new DBObjectDelete(),n,false,"user-trash-full.png");
    buttons.paint(getParent());
  }
}
