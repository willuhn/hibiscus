/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Settings.java,v $
 * $Revision: 1.17 $
 * $Date: 2004/05/11 21:11:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);
		
		LabelGroup settings = new LabelGroup(getParent(),i18n.tr("Grundeinstellungen"));

		// Einstellungen
		settings.addCheckbox(control.getOnlineMode(),i18n.tr("Keine Nachfrage vor Verbindungsaufbau"));
		settings.addCheckbox(control.getCheckPin(),i18n.tr("PIN-Eingabe via Check-Summe prüfen"));
		
		LabelGroup colors = new LabelGroup(getParent(),i18n.tr("Farben"));
		colors.addLabelPair(i18n.tr("Vordergrund Sollbuchung"),control.getBuchungSollForeground());
		colors.addLabelPair(i18n.tr("Hintergrund Sollbuchung"),control.getBuchungSollBackground());
		colors.addLabelPair(i18n.tr("Vordergrund Habenbuchung"),control.getBuchungHabenForeground());
		colors.addLabelPair(i18n.tr("Hintergrund Habenbuchung"),control.getBuchungHabenBackground());
		colors.addLabelPair(i18n.tr("Vordergrund überfällige Überweisungen"),control.getUeberfaelligForeground());
		colors.addLabelPair(i18n.tr("Hintergrund überfällige Überweisungen"),control.getUeberfaelligBackground());

		ButtonArea buttons = settings.createButtonArea(1);
		buttons.addCustomButton(i18n.tr("gespeicherte Check-Summe löschen"),new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleDeleteCheckSum();
			}
		});


		// Passports
		LabelGroup passports = new LabelGroup(getParent(),i18n.tr("Sicherheitsmedien"));

		passports.addTable(control.getPassportListe());
		
		ButtonArea buttons2 = passports.createButtonArea(1);
		buttons2.addCustomButton(i18n.tr("Neues Sicherheitsmedium anlegen"),new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
      	control.handleCreate();
      }
    });

		ButtonArea buttons3 = new ButtonArea(getParent(),2);
		buttons3.addCancelButton(control);
		buttons3.addStoreButton(control);

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.17  2004/05/11 21:11:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.14  2004/04/21 22:28:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.8  2004/02/27 01:13:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/02/27 01:12:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/02/27 01:11:53  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/25 23:11:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/