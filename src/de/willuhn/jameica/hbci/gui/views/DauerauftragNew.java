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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt die Details eines Dauerauftrages an.
 */
public class DauerauftragNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final DauerauftragControl control = new DauerauftragControl(this);

		GUI.getView().setTitle(i18n.tr("Dauerauftrag bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportDauerauftrag((Dauerauftrag) control.getTransfer())));
		
    Container konten = new SimpleContainer(getParent());
    konten.addHeadline(i18n.tr("Konten"));
		konten.addLabelPair(i18n.tr("Persönliches Konto"),			  control.getKontoAuswahl());

    ColumnLayout columns = new ColumnLayout(getParent(),2);

    {
      // Links
      Container left = new SimpleContainer(columns.getComposite());
      left.addHeadline(i18n.tr("Empfänger"));
      left.addLabelPair(i18n.tr("Name"),                      control.getEmpfaengerName());
      left.addLabelPair(i18n.tr("Kontonummer"),               control.getEmpfaengerKonto());
      left.addLabelPair(i18n.tr("BLZ"),                       control.getEmpfaengerBlz());
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
    details.addLabelPair(i18n.tr("weiterer Verwendungszweck"), control.getZweck2());
    details.addLabelPair(i18n.tr("Textschlüssel"),            control.getTextSchluessel());
	  details.addLabelPair(i18n.tr("Betrag"),                    control.getBetrag());
	  details.addSeparator();
	  details.addLabelPair(i18n.tr("Auftragsnummer"),            control.getOrderID());
  }
}
