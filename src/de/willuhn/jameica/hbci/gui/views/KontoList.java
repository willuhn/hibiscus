/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * GPLv2
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final KontoControl control = new KontoControl(this);
		GUI.getView().setTitle(i18n.tr("Vorhandene Bankverbindungen"));

    control.getKontoListe().paint(getParent());

		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Konten über den Bank-Zugang importieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReadFromPassport();
      }
    },null,false,"mail-send-receive.png");
		buttons.addButton(i18n.tr("Konto manuell anlegen"),new KontoNew(),null,false,"system-file-manager.png");
		buttons.paint(getParent());
  }
}
