/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/EmpfaengerNew.java,v $
 * $Revision: 1.20 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Empfaenger bearbeiten.
 */
public class EmpfaengerNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		GUI.getView().setTitle(i18n.tr("Adresse bearbeiten"));
		
		final EmpfaengerControl control = new EmpfaengerControl(this);

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    SimpleContainer left = new SimpleContainer(columns.getComposite());
    left.addHeadline(i18n.tr("Eigenschaften"));
    left.addLabelPair(i18n.tr("Kontoinhaber"),              control.getName());
		left.addLabelPair(i18n.tr("Kontonummer"),               control.getKontonummer());
		left.addLabelPair(i18n.tr("Bankleitzahl"),              control.getBlz());
    left.addHeadline(i18n.tr("Ausländische Bankverbindung"));
    left.addLabelPair(i18n.tr("IBAN"),                      control.getIban());
    left.addLabelPair(i18n.tr("BIC"),                       control.getBic());
    left.addLabelPair(i18n.tr("Name des Kredit-Instituts"), control.getBank());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());
    right.addLabelPair(i18n.tr("Kategorie"),control.getKategorie());

    // und noch die Abschicken-Knoepfe
    ButtonArea buttonArea = new ButtonArea();

    if (control.isHibiscusAdresse())
    {
      buttonArea.addButton(i18n.tr("Löschen"), new DBObjectDelete(),control.getAddress(),false,"user-trash-full.png");
      buttonArea.addButton(i18n.tr("Speichern"), new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleStore();
        }
      },null,true,"document-save.png");
    }
    buttonArea.paint(getParent());
      
    new Headline(getParent(),i18n.tr("Buchungen von/an diese Adresse"));
    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 100; // wir verdecken sonst den Kommentar
    folder.setLayoutData(gd);
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup tab = new TabGroup(folder,i18n.tr("Kontoauszüge"), false,1);
    control.getUmsatzListe().paint(tab.getComposite());

    // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
    TabGroup tab2 = new TabGroup(folder,i18n.tr("Eingezogene Sammel-Lastschriften"));
    control.getSammelLastListe().paint(tab2.getComposite());

    TabGroup tab3 = new TabGroup(folder,i18n.tr("Sammel-Überweisungen"));
    control.getSammelUeberweisungListe().paint(tab3.getComposite());
  }
}


/**********************************************************************
 * $Log: EmpfaengerNew.java,v $
 * Revision 1.20  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.19  2010/04/14 17:44:10  willuhn
 * @N BUGZILLA 83
 **********************************************************************/