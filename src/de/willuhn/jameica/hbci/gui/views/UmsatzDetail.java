/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzDetail.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/05/11 23:31:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bildet die Detailansicht einer Buchung ab.
 */
public class UmsatzDetail extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

    final UmsatzDetailControl control = new UmsatzDetailControl(this);
    I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
    GUI.getView().setTitle(i18n.tr("Buchungsdetails"));

    LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));

    konten.addLabelPair(i18n.tr("persönliches Konto"),		control.getKonto());
    konten.addLabelPair(i18n.tr("Name des Empfängers"),		control.getEmpfaengerName());
    konten.addLabelPair(i18n.tr("Konto des Empfängers"),	control.getEmpfaengerKonto());

    LabelGroup umsatz = new LabelGroup(getParent(),i18n.tr("Details"));
    
    umsatz.addLabelPair(i18n.tr("Betrag"),								control.getBetrag());
    umsatz.addLabelPair(i18n.tr("Datum der Buchung"),			control.getDatum());
    umsatz.addLabelPair(i18n.tr("Valuta"),								control.getValuta());
		umsatz.addSeparator();
		umsatz.addLabelPair(i18n.tr("Neuer Saldo"),						control.getSaldo());

		LabelGroup add = new LabelGroup(getParent(),i18n.tr("Zusätzliche Angaben"));
    
		add.addLabelPair(i18n.tr("Art der Buchung"),					control.getArt());
		add.addLabelPair(i18n.tr("Kundenreferenz"),						control.getCustomerRef());
		add.addLabelPair(i18n.tr("Primanota-Kennzeichen"),		control.getPrimanota());

    ButtonArea buttons = new ButtonArea(getParent(),2);
    buttons.addCustomButton(i18n.tr("Empfänger in Adressbuch übernehmen"),new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        control.handleAddEmpfaenger();
      }
    });
    buttons.addCancelButton(control);
  }
  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }
}


/**********************************************************************
 * $Log: UmsatzDetail.java,v $
 * Revision 1.5  2004/05/11 23:31:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.3  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 **********************************************************************/