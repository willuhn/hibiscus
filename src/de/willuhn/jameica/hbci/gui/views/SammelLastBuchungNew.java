/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastBuchungNew.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/07/04 12:41:39 $
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
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelLastBuchungControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung einer Buchung in einer Sammel-Lastschriften.
 */
public class SammelLastBuchungNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelLastBuchungControl control = new SammelLastBuchungControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Buchung bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Zahlungspflichtiger"));
		
		group.addLabelPair(i18n.tr("Zu belastendes Konto"),							control.getGegenKonto());
		group.addLabelPair(i18n.tr("BLZ des Zahlungspflichtigen"),			control.getGegenkontoBLZ());		
		group.addLabelPair(i18n.tr("Name des Zahlungspflichtigen"),			control.getGegenkontoName());
		group.addCheckbox(control.getStoreAddress(),i18n.tr("Adressdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());

		ButtonArea buttonArea = new ButtonArea(getParent(),3);
		buttonArea.addButton(i18n.tr("Zurück"), 				 				 new Back());
		buttonArea.addButton(i18n.tr("Löschen"),				 				 new SammelLastBuchungDelete(), control.getBuchung());
		buttonArea.addButton(i18n.tr("Speichern"), 			     		 new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: SammelLastBuchungNew.java,v $
 * Revision 1.4  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/