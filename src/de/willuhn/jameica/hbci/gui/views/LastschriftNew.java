/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/LastschriftNew.java,v $
 * $Revision: 1.15 $
 * $Date: 2008/09/29 23:48:54 $
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
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.LastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Lastschriften.
 */
public class LastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final LastschriftControl control = new LastschriftControl(this);
    final Lastschrift tranfer = (Lastschrift) control.getTransfer();

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Lastschrift bearbeiten"));
		
		LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));
		
		konten.addLabelPair(i18n.tr("persönliches Konto"),	                control.getKontoAuswahl());		
    konten.addLabelPair(i18n.tr("Kontonummer des Zahlungspflichtigen"), control.getEmpfaengerKonto());    
		konten.addLabelPair(i18n.tr("BLZ des Zahlungspflichtigen"),			    control.getEmpfaengerBlz());
    konten.addLabelPair(i18n.tr("Name des Zahlungspflichtigen"),        control.getEmpfaengerName());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Adressdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
    details.addLabelPair(i18n.tr("Textschlüssel"),            control.getTextSchluessel());
		details.addLabelPair(i18n.tr("Termin"),										control.getTermin());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addButton(i18n.tr("Zurück"), 				 				 new Back());
		buttonArea.addButton(i18n.tr("Löschen"),				 				 new DBObjectDelete(), tranfer);
		
    Button execute = new Button(i18n.tr("Jetzt ausführen"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore();
        new LastschriftExecute().handleAction(tranfer);
      }
    },null);
    execute.setEnabled(!tranfer.ausgefuehrt());
    
		Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
      	control.handleStore();
      }
    },null,true);
    store.setEnabled(!tranfer.ausgefuehrt());
    
    buttonArea.addButton(execute);
    buttonArea.addButton(store);
  }
}


/**********************************************************************
 * $Log: LastschriftNew.java,v $
 * Revision 1.15  2008/09/29 23:48:54  willuhn
 * @N Ueberfaellig-Hinweis hinter Auswahlfeld fuer Termin verschoben - spart Platz
 *
 * Revision 1.14  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.13  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.12  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.11  2006/08/23 09:57:23  willuhn
 * @C Changed default button
 *
 * Revision 1.10  2006/06/13 20:09:06  willuhn
 * @R Text "Bemerkung" entfernt
 *
 * Revision 1.9  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.8  2006/03/27 16:46:21  willuhn
 * @N GUI polish
 *
 * Revision 1.7  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.6  2005/10/17 22:00:44  willuhn
 * @B bug 143
 *
 * Revision 1.5  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.3  2005/02/19 17:22:05  willuhn
 * @B Bug 8
 *
 * Revision 1.2  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/