/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/LastschriftNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/01/19 00:16:04 $
 * $Author: willuhn $
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
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.UeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.UeberweisungExecute;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Lastschriften.
 */
public class LastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final LastschriftControl control = new LastschriftControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Lastschrift bearbeiten"));
		
		LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));
		
		konten.addLabelPair(i18n.tr("persönliches Konto (Empfänger)"),	control.getKontoAuswahl());		
		konten.addLabelPair(i18n.tr("Zu belastendes Konto"),						control.getEmpfaengerKonto());		
		konten.addLabelPair(i18n.tr("BLZ des Zahlungspflichtigen"),			control.getEmpfaengerBlz());		
		konten.addLabelPair(i18n.tr("Name des Zahlungspflichtigen"),		control.getEmpfaengerName());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Adressdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
		details.addLabelPair(i18n.tr("Termin"),										control.getTermin());

		details.addSeparator();

		details.addLabelPair(i18n.tr("Bemerkung"),								control.getComment());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addButton(i18n.tr("Zurück"), 				 				 new Back());
		buttonArea.addButton(i18n.tr("Löschen"),				 				 new UeberweisungDelete(), control.getTransfer());
		buttonArea.addButton(i18n.tr("Speichern und ausführen"), new UeberweisungExecute(),control.getTransfer());
		buttonArea.addButton(i18n.tr("Nur Speichern"), 			     new Action()
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
 * $Log: LastschriftNew.java,v $
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/