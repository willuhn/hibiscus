/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Settings.java,v $
 * $Revision: 1.41 $
 * $Date: 2007/06/12 08:56:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Session;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * In der Session merken wir uns das letzte aktive Tab
   */
  private static Session session = null;

  /**
   * Der Tabfolder.
   */
  private TabFolder folder = null;

  /**
   * ct.
   */
  public Settings()
  {
    if (session == null)
      session = new Session(30 * 60 * 1000l);
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);
		
		// Grund-Einstellungen
    TabGroup system = new TabGroup(getTabFolder(),i18n.tr("Grundeinstellungen"));
		system.addCheckbox(control.getOnlineMode(),i18n.tr("Dauerhafte Internetverbindung, Aufforderung zum Verbinden nicht erforderlich"));
    system.addCheckbox(control.getCheckPin(),i18n.tr("PIN-Eingaben via Check-Summe prüfen"));
    system.addCheckbox(control.getCachePin(),i18n.tr("PIN-Eingaben für die aktuelle Sitzung zwischenspeichern"));
    system.addCheckbox(control.getCancelSyncOnError(),i18n.tr("HBCI-Synchronisierung bei Fehler abbrechen"));
    system.addCheckbox(control.getDecimalGrouping(),i18n.tr("Tausender-Trennzeichen bei Geld-Beträgen verwenden"));
    system.addCheckbox(control.getKontoCheck(),i18n.tr("Kontonummern via Prüfsumme der Bank testen"));
    system.addLabelPair(i18n.tr("Limit für Aufträge"), control.getUeberweisungLimit());
		
    ButtonArea sysbuttons = system.createButtonArea(1);
    sysbuttons.addButton(i18n.tr("gespeicherte Prüfsummen löschen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleDeleteCheckSum();
      }
    });

    // Farb-Einstellungen
    TabGroup colors = new TabGroup(getTabFolder(),i18n.tr("Farben"));
    colors.addLabelPair(i18n.tr("Vordergrund Sollbuchung"),control.getBuchungSollForeground());
    colors.addLabelPair(i18n.tr("Vordergrund Habenbuchung"),control.getBuchungHabenForeground());
		colors.addLabelPair(i18n.tr("Vordergrund überfällige Überweisungen"),control.getUeberfaelligForeground());

		// Passports
    TabGroup passports = new TabGroup(getTabFolder(),i18n.tr("HBCI-Sicherheitsmedien"));
		passports.addPart(control.getPassportListe());
    ButtonArea passportButtons = passports.createButtonArea(1);
    passportButtons.addButton(i18n.tr("Sicherheitsmedium konfigurieren..."), new Action() {
    
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          new PassportDetail().handleAction(control.getPassportListe().getSelection());
        }
        catch (RemoteException re)
        {
          Logger.error("unable to load passport",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Öffnen des Sicherheitsmediums"), StatusBarMessage.TYPE_ERROR));
        }
      }
    });

    // Umsatz-Kategorien
    TabGroup umsatztypes = new TabGroup(getTabFolder(),i18n.tr("Umsatz-Kategorien"));
    control.getUmsatzTypListe().paint(umsatztypes.getComposite()); // BUGZILLA 410
    ButtonArea umsatzButtons = umsatztypes.createButtonArea(1);
    umsatzButtons.addButton(i18n.tr("Neue Umsatz-Kategorie..."),new UmsatzTypNew());

    ButtonArea buttons = new ButtonArea(getParent(),2);
		buttons.addButton(i18n.tr("Zurück"),new Back());
		buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    });

    // Mal checken, ob wir uns das zuletzt aktive Tab gemerkt haben.
    Integer selection = (Integer) session.get("active");
    if (selection != null)
      getTabFolder().setSelection(selection.intValue());
  }

  /**
   * Liefert den Tab-Folder, in dem die einzelnen Module der Einstellungen
   * untergebracht sind.
   * @return der Tab-Folder.
   */
  public TabFolder getTabFolder()
  {
    if (this.folder != null)
      return this.folder;
    
    this.folder = new TabFolder(getParent(), SWT.NONE);
    this.folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.folder.setBackground(Color.BACKGROUND.getSWTColor());
    return this.folder;
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    // Wir merken uns das aktive Tab fuer eine Weile, damit wir das gleich
    // wieder anzeigen koennen, wenn der User zurueckkommt.
    session.put("active",new Integer(getTabFolder().getSelectionIndex()));
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.41  2007/06/12 08:56:01  willuhn
 * @B Bug 410
 *
 * Revision 1.40  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.39  2007/02/26 11:40:06  willuhn
 * @C Ergonomie-Vorschlag von Gottfried
 *
 * Revision 1.38  2006/11/24 00:07:09  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.37  2006/10/06 13:08:01  willuhn
 * @B Bug 185, 211
 *
 * Revision 1.36  2006/08/03 15:32:35  willuhn
 * @N Bug 62
 *
 * Revision 1.35  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.34  2006/01/08 23:23:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
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