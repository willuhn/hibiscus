/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UeberweisungNeu.java,v $
 * $Revision: 1.4 $
 * $Date: 2004/03/04 00:35:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Ueberweisungen.
 */
public class UeberweisungNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.setTitleText(i18n.tr("Überweisung bearbeiten"));
		
		UeberweisungControl control = new UeberweisungControl(this);
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
		
		group.addLabelPair(i18n.tr("Konto"),										control.getKontoAuswahl());		
		group.addLabelPair(i18n.tr("Konto des Empfängers"),			control.getEmpfaengerKonto());		
		group.addLabelPair(i18n.tr("BLZ des Empfängers"),				control.getEmpfaengerBlz());		
		group.addLabelPair(i18n.tr("Name des Empfängers"),			control.getEmpfaengerName());
		group.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Empfängerdaten im Adressbuch speichern"));

		group.addSeparator();

		group.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		group.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		group.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());


		ButtonArea buttonArea = new ButtonArea(getParent(),3);
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
    // TODO Auto-generated method stub

  }

}


/**********************************************************************
 * $Log: UeberweisungNeu.java,v $
 * Revision 1.4  2004/03/04 00:35:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/