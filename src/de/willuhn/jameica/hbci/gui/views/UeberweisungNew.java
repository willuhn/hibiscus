/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UeberweisungNew.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/02/03 18:57:42 $
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
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Ueberweisungen.
 */
public class UeberweisungNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final UeberweisungControl control = new UeberweisungControl(this);
    final Transfer transfer = control.getTransfer();

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Überweisung bearbeiten"));
		
		LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));
		
		konten.addLabelPair(i18n.tr("persönliches Konto"),				control.getKontoAuswahl());		
		konten.addLabelPair(i18n.tr("Konto des Empfängers"),			control.getEmpfaengerKonto());		
		konten.addLabelPair(i18n.tr("BLZ des Empfängers"),				control.getEmpfaengerBlz());		
		konten.addLabelPair(i18n.tr("Name des Empfängers"),			  control.getEmpfaengerName());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Empfängerdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
		details.addLabelPair(i18n.tr("Termin"),										control.getTermin());

		details.addSeparator();

		details.addLabelPair(i18n.tr("Bemerkung"),								control.getComment());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addButton(i18n.tr("Zurück"), 				 				 new Back());
		buttonArea.addButton(i18n.tr("Löschen"),				 				 new UeberweisungDelete(), transfer);
		buttonArea.addButton(i18n.tr("Speichern und ausführen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				control.handleStore();
				new UeberweisungExecute().handleAction(transfer);
      }
    },null,true);
		buttonArea.addButton(i18n.tr("Nur Speichern"), 			     new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: UeberweisungNew.java,v $
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 18:19:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2004/10/29 16:16:12  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.19  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/10/21 14:05:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.14  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.12  2004/05/04 23:58:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/04 23:07:24  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.10  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.9  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.8  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.6  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
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