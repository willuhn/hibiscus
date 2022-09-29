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
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragDelete;
import de.willuhn.jameica.hbci.gui.controller.SepaDauerauftragControl;
import de.willuhn.jameica.hbci.gui.parts.PanelButtonNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt die Details eines SEPA-Dauerauftrages an.
 */
public class SepaDauerauftragNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final SepaDauerauftragControl control = new SepaDauerauftragControl(this);
    final SepaDauerauftrag da = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("SEPA-Dauerauftrag bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonNew(SepaDauerauftrag.class));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSepaDauerauftrag(da)));

    Container konten = new SimpleContainer(getParent());
    konten.addHeadline(i18n.tr("Konten"));
		konten.addLabelPair(i18n.tr("Persönliches Konto"),			  control.getKontoAuswahl());

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    {
      // Links
      Container left = new SimpleContainer(columns.getComposite());
      left.addHeadline(i18n.tr("Empfänger"));
      left.addLabelPair(i18n.tr("Name"),                      control.getEmpfaengerName());
      left.addLabelPair(i18n.tr("IBAN"),                      control.getEmpfaengerKonto());    
      left.addLabelPair(i18n.tr("BIC"),                       control.getEmpfaengerBic());
      left.addInput(control.getStoreEmpfaenger());
    }
    {
      // Rechts
      Container right = new SimpleContainer(columns.getComposite());
      right.addHeadline(i18n.tr("Turnus"));
      right.addLabelPair(i18n.tr("Zahlungsturnus"),           control.getTurnus());
      right.addLabelPair(i18n.tr("Erste Zahlung"),            control.getErsteZahlung());
      right.addLabelPair(i18n.tr("Letzte Zahlung"),           control.getLetzteZahlung());
    }
		
    Container details = new SimpleContainer(getParent());
    details.addHeadline(i18n.tr("Details"));
    details.addLabelPair(i18n.tr("Verwendungszweck"),          control.getZweck());
	  details.addLabelPair(i18n.tr("Betrag"),                    control.getBetrag());
	  details.addSeparator();
    details.addInput(control.getPurposeCode());
	  details.addLabelPair(i18n.tr("Auftragsnummer"),            control.getOrderID());
    
		ButtonArea buttonArea = new ButtonArea();
		String s = i18n.tr("Jetzt ausführen...");
		if (da.isActive())
			s = i18n.tr("Jetzt aktualisieren...");

		buttonArea.addButton(i18n.tr("Löschen"),	 		 new SepaDauerauftragDelete(), da, false,"user-trash-full.png");
		buttonArea.addButton(s,										 		 new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleExecute();
      }
    },null,false,"emblem-important.png");
		buttonArea.addButton(i18n.tr("&Speichern"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.handleStore();
			}
    },null,true,"document-save.png");
		
		buttonArea.paint(getParent());
  }
}
