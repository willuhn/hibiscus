/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
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
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
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
    left.addHeadline(i18n.tr("Nationale Bankverbindung"));
    left.addLabelPair(i18n.tr("Kontoinhaber"),              control.getName());
		left.addLabelPair(i18n.tr("Kontonummer"),               control.getKontonummer());
		left.addLabelPair(i18n.tr("Bankleitzahl"),              control.getBlz());
    left.addHeadline(i18n.tr("Europäische Bankverbindung (SEPA)"));
    left.addLabelPair(i18n.tr("IBAN"),                      control.getIban());
    left.addLabelPair(i18n.tr("BIC"),                       control.getBic());
    left.addLabelPair(i18n.tr("Name des Kredit-Instituts"), control.getBank());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Notiz"));
    right.addPart(control.getKommentar());
    right.addLabelPair(i18n.tr("Gruppe"),control.getKategorie());

    // und noch die Abschicken-Knoepfe
    ButtonArea buttonArea = new ButtonArea();
    
    Button delete = new Button(i18n.tr("Löschen"), new DBObjectDelete(),control.getAddress(),false,"user-trash-full.png");
    delete.setEnabled(control.isHibiscusAdresse());
    buttonArea.addButton(delete);
    
    Button store = new Button(i18n.tr("&Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true,"document-save.png");
    store.setEnabled(control.isHibiscusAdresse());
    buttonArea.addButton(store);

    buttonArea.paint(getParent());
      
    new Headline(getParent(),i18n.tr("Buchungen von/an diese Adresse"));
    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 100; // wir verdecken sonst den Kommentar
    folder.setLayoutData(gd);

    TabGroup tab = new TabGroup(folder,i18n.tr("Umsätze"), false,1);
    control.getUmsatzListe().paint(tab.getComposite());

    TabGroup tab2 = new TabGroup(folder,i18n.tr("Überweisungen"));
    control.getUeberweisungListe().paint(tab2.getComposite());

    TabGroup tab1 = new TabGroup(folder,i18n.tr("Lastschriften"));
    control.getLastschriftListe().paint(tab1.getComposite());

    TabGroup tab3 = new TabGroup(folder,i18n.tr("Sammellastschriften"));
    control.getSammelLastListe().paint(tab3.getComposite());

    TabGroup tab4 = new TabGroup(folder,i18n.tr("Sammelüberweisungen"));
    control.getSammelUeberweisungListe().paint(tab4.getComposite());
  }
}
