/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Settings.java,v $
 * $Revision: 1.49 $
 * $Date: 2011/04/08 15:19:14 $
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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Wir merken uns das letzte aktive Tab
   */
  private static Integer lastActiveTab = null;

  /**
   * Der Tabfolder.
   */
  private TabFolder folder = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		GUI.getView().setTitle(i18n.tr("Einstellungen"));
		final SettingsControl control = new SettingsControl(this);
		
		// Grund-Einstellungen
    TabGroup system = new TabGroup(getTabFolder(),i18n.tr("Grundeinstellungen"));
		system.addCheckbox(control.getOnlineMode(),i18n.tr("Dauerhafte Internetverbindung, Aufforderung zum Verbinden nicht erforderlich"));
    system.addCheckbox(control.getCachePin(),i18n.tr("PIN-Eingaben für die aktuelle Sitzung zwischenspeichern"));
    system.addCheckbox(control.getCancelSyncOnError(),i18n.tr("HBCI-Synchronisierung bei Fehler abbrechen"));
    system.addCheckbox(control.getDecimalGrouping(),i18n.tr("Tausender-Trennzeichen bei Geld-Beträgen anzeigen"));
    system.addCheckbox(control.getKontoCheck(),i18n.tr("Kontonummern und Bankleitzahlen mittels Prüfsumme testen"));
    system.addLabelPair(i18n.tr("Limit für Aufträge"), control.getUeberweisungLimit());
		
    // Farb-Einstellungen
    TabGroup colors = new TabGroup(getTabFolder(),i18n.tr("Farben"));
    colors.addLabelPair(i18n.tr("Vordergrund Sollbuchung"),control.getBuchungSollForeground());
    colors.addLabelPair(i18n.tr("Vordergrund Habenbuchung"),control.getBuchungHabenForeground());
		colors.addLabelPair(i18n.tr("Vordergrund überfällige Überweisungen"),control.getUeberfaelligForeground());

		// Passports
    TabGroup passports = new TabGroup(getTabFolder(),i18n.tr("HBCI-Sicherheitsmedien"));
		passports.addPart(control.getPassportListe());
		
    ButtonArea passportButtons = new ButtonArea();
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
    },null,false,"document-properties.png");
    passports.addButtonArea(passportButtons);

    // Umsatz-Kategorien
    TabGroup umsatztypes = new TabGroup(getTabFolder(),i18n.tr("Umsatz-Kategorien"));
    control.getUmsatzTypTree().paint(umsatztypes.getComposite()); // BUGZILLA 410
    ButtonArea umsatzButtons = new ButtonArea();
    umsatzButtons.addButton(i18n.tr("Neue Umsatz-Kategorie..."),new UmsatzTypNew(),null,false,"text-x-generic.png");
    umsatztypes.addButtonArea(umsatzButtons);

    ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null,true,"document-save.png");
		buttons.paint(getParent());

    // Mal checken, ob wir uns das zuletzt aktive Tab gemerkt haben.
    if (lastActiveTab != null)
      getTabFolder().setSelection(lastActiveTab.intValue());
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
    // Wir merken uns das aktive Tab
    lastActiveTab = new Integer(getTabFolder().getSelectionIndex());
  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.49  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.48  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 **********************************************************************/