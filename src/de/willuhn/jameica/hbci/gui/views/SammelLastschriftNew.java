/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftNew.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/03/02 00:22:05 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelLastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelLastschriftControl control = new SammelLastschriftControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Sammel-Lastschrift bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("persönliches Konto (Empfänger)"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addLabelPair(i18n.tr("Termin"),control.getTermin());
		
		group.addSeparator();
		group.addLabelPair(i18n.tr("Bemerkung"),control.getComment());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());


    ButtonArea buttons = new ButtonArea(getParent(),4);
    buttons.addButton(i18n.tr("Zurück"),new Back());
    buttons.addButton(i18n.tr("Neue Buchung hinzufügen"), new SammelLastBuchungNew(),control.getLastschrift());
    buttons.addButton(i18n.tr("Sammel-Lastschrift löschen"),new SammelLastschriftDelete(),control.getLastschrift());
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: SammelLastschriftNew.java,v $
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/