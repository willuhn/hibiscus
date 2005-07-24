/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Settings.java,v $
 * $Revision: 1.32 $
 * $Date: 2005/07/24 22:26:42 $
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
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);
		
		LabelGroup settings = new LabelGroup(getParent(),i18n.tr("Grundeinstellungen"));

		// Einstellungen
		settings.addCheckbox(control.getOnlineMode(),i18n.tr("Bei Kommunikation mit der Bank Internetverbindung ohne Nachfrage herstellen"));
		settings.addCheckbox(control.getCheckPin(),i18n.tr("PIN-Eingabe via Check-Summe prüfen"));
    settings.addCheckbox(control.getShowTan(),i18n.tr("TANs während der Eingabe anzeigen"));
    settings.addCheckbox(control.getDecimalGrouping(),i18n.tr("Tausender-Trennzeichen bei Geld-Beträgen verwenden"));
		
		settings.addLabelPair(i18n.tr("Limit für Aufträge"), control.getUeberweisungLimit());
		
		LabelGroup colors = new LabelGroup(getParent(),i18n.tr("Farben"));
		colors.addLabelPair(i18n.tr("Vordergrund Sollbuchung"),control.getBuchungSollForeground());
		colors.addLabelPair(i18n.tr("Vordergrund Habenbuchung"),control.getBuchungHabenForeground());
		colors.addLabelPair(i18n.tr("Vordergrund überfällige Überweisungen"),control.getUeberfaelligForeground());

		ButtonArea buttons = settings.createButtonArea(1);
		buttons.addButton(i18n.tr("gespeicherte Check-Summen löschen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				control.handleDeleteCheckSum();
      }
    });

		// Passports
		LabelGroup passports = new LabelGroup(getParent(),i18n.tr("Sicherheitsmedien"));
		passports.addPart(control.getPassportListe());

		ButtonArea buttons3 = new ButtonArea(getParent(),2);
		buttons3.addButton(i18n.tr("Zurück"),new Back());
		buttons3.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    });

  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.32  2005/07/24 22:26:42  web0
 * @B bug 101
 *
 * Revision 1.31  2005/06/06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.30  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.29  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.28  2005/01/30 20:45:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.27  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.26  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.25  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.23  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.21  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.20  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.19  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.18  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
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