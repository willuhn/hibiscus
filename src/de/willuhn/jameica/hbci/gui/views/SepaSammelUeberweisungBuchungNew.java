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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.SepaSammelUeberweisungBuchungControl;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung einer Buchung in einer SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungBuchungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final SepaSammelUeberweisungBuchungControl control = new SepaSammelUeberweisungBuchungControl(this);
    final SepaSammelTransfer l = control.getBuchung().getSammelTransfer();
    
    GUI.getView().setTitle(i18n.tr("SEPA-Sammelüberweisung {0}: Buchung bearbeiten",l.getBezeichnung()));

    // Zusaetzlicher Back-Button, um zurueck zum Auftrag zu kommen
    GUI.getView().addPanelButton(new PanelButton("sueberweisung.png",new de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungNew(){
      public void handleAction(Object context) throws ApplicationException
      {
        super.handleAction(l);
      }
    },i18n.tr("Zurück zum Sammelauftrag")));
		

    ColumnLayout cols = new ColumnLayout(getParent(),2);
    
    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Empfänger"));
      container.addLabelPair(i18n.tr("Name"), control.getEmpfaengerName());
      container.addLabelPair(i18n.tr("IBAN"), control.getEmpfaengerKonto());    
      container.addLabelPair(i18n.tr("BIC"),  control.getEmpfaengerBic());
      container.addInput(control.getStoreEmpfaenger());
    }
    
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("SEPA"));
      container.addInput(control.getEndToEndId());
      container.addInput(control.getPurposeCode());
    }

    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Details"));
    container.addLabelPair(i18n.tr("Verwendungszweck"), control.getZweck());
    container.addLabelPair(i18n.tr("Betrag"),           control.getBetrag());
    
		ButtonArea buttonArea = new ButtonArea();
    Button delete = new Button(i18n.tr("Löschen"), new DBObjectDelete(),control.getBuchung(),false,"user-trash-full.png");
    delete.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(delete);

    Button store = new Button(i18n.tr("&Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore();
      }
    },null,false,"document-save.png");
    store.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store);
    
    // BUGZILLA 116 http://www.willuhn.de/bugzilla/show_bug.cgi?id=116
    Button store2 = new Button(i18n.tr("Speichern und nächste Buchung"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
        {
          new de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungBuchungNew().handleAction(l);
          // Wir schicken das hier nochmal, weil beim Start einer neuen View die Statusbar geleert wird
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Buchung gespeichert"),StatusBarMessage.TYPE_SUCCESS));
        }
      }
    },null,!l.ausgefuehrt(),"go-next.png");
    store2.setEnabled(!l.ausgefuehrt());
    buttonArea.addButton(store2);
    
    buttonArea.paint(getParent());
  }
}
