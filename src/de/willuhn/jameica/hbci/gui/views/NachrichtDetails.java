/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/NachrichtDetails.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/07/17 08:42:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
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
		
    SimpleContainer container = new SimpleContainer(getParent());
    container.addText(i18n.tr("{0} [BLZ: {1}]", new String[] {HBCIUtils.getNameForBLZ(n.getBLZ()),n.getBLZ()}) + "\n",true);
    container.addText(n.getNachricht(),true);

    ButtonArea buttons = container.createButtonArea(3);
    buttons.addButton(new Back(true));
    buttons.addButton(i18n.tr("In Zwischenablage kopieren"),new NachrichtCopy(),n,false,"edit-copy.png");
    buttons.addButton(i18n.tr("Löschen"),new DBObjectDelete(),n,false,"user-trash-full.png");
  }
}


/**********************************************************************
 * $Log: NachrichtDetails.java,v $
 * Revision 1.1  2009/07/17 08:42:57  willuhn
 * @N Detail-Ansicht fuer Systemnachrichten der Bank
 * @N Systemnachrichten in Zwischenablage kopieren
 *
 **********************************************************************/