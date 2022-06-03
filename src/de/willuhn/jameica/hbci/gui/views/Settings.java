/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView implements Extendable
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
    system.addHeadline(i18n.tr("Sicherheit"));
    system.addCheckbox(control.getCachePin(),i18n.tr("PIN-Eingaben für die aktuelle Sitzung zwischenspeichern"));
    system.addCheckbox(control.getStorePin(),i18n.tr("PIN-Eingaben permanent speichern (nur bei PIN/TAN)"));
    system.addHeadline(i18n.tr("Kontrolle"));
    system.addCheckbox(control.getKontoCheck(),i18n.tr("Kontonummern und Bankleitzahlen mittels Prüfsumme testen"));
    system.addCheckbox(control.getKontoCheckExcludeAddressbook(),i18n.tr("Außer Bankverbindungen des Adressbuches"));
    system.addLabelPair(i18n.tr("Limit für Aufträge"), control.getUeberweisungLimit());

    // Farb-Einstellungen
    TabGroup ui = new TabGroup(getTabFolder(),i18n.tr("Benutzeroberfläche"));
    ui.addHeadline(i18n.tr("Farben"));
    ui.addLabelPair(i18n.tr("Textfarbe von Sollbuchungen"),control.getBuchungSollForeground());
    ui.addLabelPair(i18n.tr("Textfarbe von Habenbuchungen"),control.getBuchungHabenForeground());
    ui.addCheckbox(control.getColorValues(),i18n.tr("Nur Geld-Beträge farbig anzeigen"));
    ui.addHeadline(i18n.tr("Formatierung"));
    ui.addCheckbox(control.getDecimalGrouping(),i18n.tr("Tausender-Trennzeichen bei Geld-Beträgen anzeigen"));
    ui.addCheckbox(control.getBoldValues(),i18n.tr("Geld-Beträge fett gedruckt anzeigen"));

    // Umsatz-Kategorien
    TabGroup umsatztypes = new TabGroup(getTabFolder(),i18n.tr("Umsatz-Kategorien"));
    control.getUmsatzTypTree().paint(umsatztypes.getComposite()); // BUGZILLA 410
    ButtonArea umsatzButtons = new ButtonArea();
    umsatzButtons.addButton(i18n.tr("Neue Umsatz-Kategorie..."),new UmsatzTypNew(),null,false,"text-x-generic.png");
    umsatztypes.addButtonArea(umsatzButtons);

    // anzuzeigende Zeiträume in der Vorauswahl
    TabGroup ranges = new TabGroup(getTabFolder(),i18n.tr("Zeiträume"), true,1);
    ranges.addText(i18n.tr("Wählen Sie für die verschiedenen Bereiche der Anwendung aus, welche Zeitraum-Vorauswahlen angezeigt werden sollen."),true);
    
    ranges.addHeadline(i18n.tr("Für Zahlungsverkehr"));
    ranges.addPart(control.getRanges(Range.CATEGORY_ZAHLUNGSVERKEHR));
    ranges.addHeadline(i18n.tr("Für Auswertungen, Umsatzlisten und Kontoauszüge"));
    ranges.addPart(control.getRanges(Range.CATEGORY_AUSWERTUNG));

    TabGroup extended = new TabGroup(getTabFolder(),"Erweitert",true);
    extended.addHeadline(i18n.tr("Experimentelle Funktionen"));
    extended.addText(i18n.tr("Wenn die experimentellen Funktionen nicht aktiviert sind, gelten die Vorgabewerte.") + "\n",true);
    extended.addInput(control.getExFeatures());
    extended.addSeparator();
    for (CheckboxInput c:control.getExperiments())
    {
      extended.addInput(c);
      extended.addText((String) c.getData("description") + "\n",true, Color.COMMENT);
    }

    ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("&Speichern"),new Action()
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
    return this.folder;
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    // Wir merken uns das aktive Tab
    lastActiveTab = Integer.valueOf(getTabFolder().getSelectionIndex());
  }
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

}
