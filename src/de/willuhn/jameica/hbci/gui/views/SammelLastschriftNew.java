/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftNew.java,v $
 * $Revision: 1.23 $
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

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelLastschriftNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final SammelLastschriftControl control = new SammelLastschriftControl(this);
    SammelTransfer transfer = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("Sammel-Lastschrift bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSammelLastschrift(transfer)));
		
		Container group = new SimpleContainer(getParent());
		group.addHeadline(i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Gutschriftskonto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addInput(control.getTermin());
		
		group.addSeparator();
    group.addLabelPair(i18n.tr("Summe der Buchungen"),control.getSumme());

		final SammelLastschrift l = (SammelLastschrift) control.getTransfer();

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Löschen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new DBObjectDelete().handleAction(context);
        try
        {
          // Buchungen aus der Liste entfernen
          control.getBuchungen().removeAll();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to remove bookings",re);
        }
      }
    },control.getTransfer(),false,"user-trash-full.png");

    Button add = new Button(i18n.tr("Neue Buchungen hinzufügen"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
          new SammelLastBuchungNew().handleAction(l);
      }
    },null,false,"text-x-generic.png");
    add.setEnabled(!transfer.ausgefuehrt());
    
		Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
			public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
  				new SammelLastschriftExecute().handleAction(l);
			}
		},null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
    Button store = new Button(i18n.tr("Speichern"),new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttons.addButton(add);
    buttons.addButton(execute);
    buttons.addButton(store);
    
    buttons.paint(getParent());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());
  }
}


/**********************************************************************
 * $Log: SammelLastschriftNew.java,v $
 * Revision 1.23  2011/06/24 07:55:41  willuhn
 * @C Bei Hibiscus-verwalteten Terminen besser "Fällig am" verwenden - ist nicht so missverstaendlich - der User denkt sonst ggf. es sei ein bankseitig terminierter Auftrag
 *
 * Revision 1.22  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.21  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.20  2010-12-13 11:01:08  willuhn
 * @B Wenn man einen Sammelauftrag in der Detailansicht loeschte, konnte man anschliessend noch doppelt auf die zugeordneten Buchungen klicken und eine ObjectNotFoundException ausloesen
 **********************************************************************/