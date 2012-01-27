/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/LastschriftNew.java,v $
 * $Revision: 1.25 $
 * $Date: 2012/01/27 22:43:22 $
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
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.LastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportLastschrift;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Lastschriften.
 */
public class LastschriftNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final LastschriftControl control = new LastschriftControl(this);
    final Lastschrift transfer = (Lastschrift) control.getTransfer();


		GUI.getView().setTitle(i18n.tr("Lastschrift bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportLastschrift(transfer)));
		
    Container c1 = new SimpleContainer(getParent());
    c1.addHeadline(i18n.tr("Konto"));
    c1.addInput(control.getKontoAuswahl());

    ColumnLayout cols = new ColumnLayout(getParent(),2);

    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Zahlungspflichtiger"));
      container.addInput(control.getEmpfaengerName());
      container.addInput(control.getEmpfaengerKonto());
      container.addInput(control.getEmpfaengerBlz());
      container.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("In Adressbuch übernehmen"));
    }
    
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Sonstige Informationen"));
      container.addInput(control.getTextSchluessel());
      container.addInput(control.getTermin());
      container.addInput(control.getReminderInterval());
    }
    
    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Details"));
    container.addInput(control.getZweck());
    container.addInput(control.getZweck2());
    container.addInput(control.getBetrag());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("Löschen"), new DBObjectDelete(),transfer,false,"user-trash-full.png");
    buttonArea.addButton(i18n.tr("Duplizieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore()) // BUGZILLA 1181
          new Duplicate().handleAction(transfer);
      }
    },null,false,"edit-copy.png");
		
    Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore()) // BUGZILLA 661
          new LastschriftExecute().handleAction(transfer);
      }
    },null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
		Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
      	control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttonArea.addButton(execute);
    buttonArea.addButton(store);
    
    buttonArea.paint(getParent());
  }
}


/**********************************************************************
 * $Log: LastschriftNew.java,v $
 * Revision 1.25  2012/01/27 22:43:22  willuhn
 * @N BUGZILLA 1181
 *
 * Revision 1.24  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 * Revision 1.23  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.22  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.21  2010-08-17 11:41:45  willuhn
 * @N Duplizieren-Button auch in der Detail-Ansicht
 *
 * Revision 1.20  2010-08-17 11:32:11  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.19  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.18  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.17  2009/01/20 10:51:45  willuhn
 * @N Mehr Icons - fuer Buttons
 **********************************************************************/