/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/AuslandsUeberweisungNew.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/06/24 07:55:41 $
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
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.AuslandsUeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportAuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Auslands-Ueberweisungen.
 */
public class AuslandsUeberweisungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final AuslandsUeberweisungControl control = new AuslandsUeberweisungControl(this);
    final AuslandsUeberweisung transfer = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("SEPA-Überweisung bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportAuslandsUeberweisung(transfer)));
		
    SimpleContainer konten = new SimpleContainer(getParent());
    konten.addHeadline(i18n.tr("Konto"));
		konten.addLabelPair(i18n.tr("Persönliches Konto"),        control.getKontoAuswahl());
    konten.addHeadline(i18n.tr("Empfänger"));
    konten.addLabelPair(i18n.tr("Name"),                      control.getEmpfaengerName());
    konten.addLabelPair(i18n.tr("IBAN"),                      control.getEmpfaengerKonto());    
    konten.addLabelPair(i18n.tr("BIC"),                       control.getEmpfaengerBic());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("In Adressbuch übernehmen"));

    SimpleContainer details = new SimpleContainer(getParent());
    details.addHeadline(i18n.tr("Details"));
		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
    details.addLabelPair(i18n.tr("Betrag"),                   control.getBetrag());
    details.addInput(control.getTermin());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("Löschen"),new DBObjectDelete(),transfer,false,"user-trash-full.png");

    Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException {
				if (control.handleStore())
  				new AuslandsUeberweisungExecute().handleAction(transfer);
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
 * $Log: AuslandsUeberweisungNew.java,v $
 * Revision 1.8  2011/06/24 07:55:41  willuhn
 * @C Bei Hibiscus-verwalteten Terminen besser "Fällig am" verwenden - ist nicht so missverstaendlich - der User denkt sonst ggf. es sei ein bankseitig terminierter Auftrag
 *
 * Revision 1.7  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.6  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.5  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.4  2009/05/07 15:13:37  willuhn
 * @N BIC in Auslandsueberweisung
 *
 * Revision 1.3  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
 *
 * Revision 1.2  2009/03/17 23:50:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 **********************************************************************/