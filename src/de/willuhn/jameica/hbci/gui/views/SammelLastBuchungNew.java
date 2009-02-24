/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastBuchungNew.java,v $
 * $Revision: 1.12 $
 * $Date: 2009/02/24 23:51:01 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelLastBuchungControl;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
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

    SammelTransfer l = control.getBuchung().getSammelTransfer();
    GUI.getView().setTitle(i18n.tr("Buchung bearbeiten [{0}]",l.getBezeichnung()));
		
    SimpleContainer group = new SimpleContainer(getParent());
    group.addHeadline(i18n.tr("Zahlungspflichtiger"));
    group.addLabelPair(i18n.tr("Name"),                       control.getGegenkontoName());
    group.addLabelPair(i18n.tr("Kontonummer"),	              control.getGegenKonto());
		group.addLabelPair(i18n.tr("BLZ"),			                  control.getGegenkontoBLZ());
		group.addCheckbox(control.getStoreAddress(),i18n.tr("In Adressbuch übernehmen"));

    SimpleContainer details = new SimpleContainer(getParent());
    details.addHeadline(i18n.tr("Details"));
		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
    details.addLabelPair(i18n.tr("Textschlüssel"),            control.getTextSchluessel());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
    buttonArea.addButton(new Back(l.ausgefuehrt()));
    Button delete = new Button(i18n.tr("Löschen"), new SammelTransferBuchungDelete(),control.getBuchung());
    delete.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(delete);

    Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore(false);
      }
    });
    store.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store);
    
    // BUGZILLA 116 http://www.willuhn.de/bugzilla/show_bug.cgi?id=116
    Button store2 = new Button(i18n.tr("Speichern und nächste Buchung"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore(true);
      }
    },null,!l.ausgefuehrt());
    store2.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store2);
  }
}


/**********************************************************************
 * $Log: SammelLastBuchungNew.java,v $
 * Revision 1.12  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.11  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.10  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.9  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.8  2006/03/27 16:46:21  willuhn
 * @N GUI polish
 *
 * Revision 1.7  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.6  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.5  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
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