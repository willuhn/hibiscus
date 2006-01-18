/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelUeberweisungBuchungNew.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/18 00:51:00 $
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
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelUeberweisungBuchungControl;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung einer Buchung in einer Sammel-Lastschriften.
 */
public class SammelUeberweisungBuchungNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelUeberweisungBuchungControl control = new SammelUeberweisungBuchungControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    SammelTransfer l = control.getBuchung().getSammelTransfer();
    Integer i = (Integer) l.getAttribute("anzahl");
    GUI.getView().setTitle(i18n.tr("Buchung bearbeiten [Nr. {0}]",String.valueOf(i.intValue() + 1)));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Empfänger"));
		
		group.addLabelPair(i18n.tr("Konto"),		control.getGegenKonto());
		group.addLabelPair(i18n.tr("BLZ"),			control.getGegenkontoBLZ());		
		group.addLabelPair(i18n.tr("Name"),			control.getGegenkontoName());
		group.addCheckbox(control.getStoreAddress(),i18n.tr("Adressdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addButton(i18n.tr("Zurück"), 				 				       new Back());
		buttonArea.addButton(i18n.tr("Löschen"),				 				       new SammelTransferBuchungDelete(), control.getBuchung());
		buttonArea.addButton(i18n.tr("Speichern"), 			     		 new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore(false);
      }
    });
    // BUGZILLA 116 http://www.willuhn.de/bugzilla/show_bug.cgi?id=116
    buttonArea.addButton(i18n.tr("Speichern und nächste Buchung"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore(true);
      }
    },null,true);
  }
}


/**********************************************************************
 * $Log: SammelUeberweisungBuchungNew.java,v $
 * Revision 1.2  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/