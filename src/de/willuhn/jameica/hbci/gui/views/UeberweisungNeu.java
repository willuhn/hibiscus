/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UeberweisungNeu.java,v $
 * $Revision: 1.13 $
 * $Date: 2004/07/20 22:53:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
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

		final UeberweisungControl control = new UeberweisungControl(this);

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Überweisung bearbeiten"));
		
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
		group.addLabelPair(i18n.tr("Termin"),										control.getTermin());

		group.addSeparator();

		group.addLabelPair(i18n.tr("Bemerkung"),								control.getComment());

		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addCustomButton(i18n.tr("jetzt ausführen"), new Listener()
    {
      public void handleEvent(Event event)
      {
				control.handleExecute();
      }
    });
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: UeberweisungNeu.java,v $
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