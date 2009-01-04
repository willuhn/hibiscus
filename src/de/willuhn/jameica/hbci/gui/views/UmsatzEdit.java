/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UmsatzEdit.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/01/04 01:25:47 $
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
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailEditControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bildet die Edit-Ansicht einer Buchung ab.
 */
public class UmsatzEdit extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

    final UmsatzDetailEditControl control = new UmsatzDetailEditControl(this);
    final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // BUGZILLA 38 http://www.willuhn.de/bugzilla/show_bug.cgi?id=38
    Konto k = control.getUmsatz().getKonto();

    String s1 = k.getLongName();
    if (s1 == null) s1 = "";

    double d = k.getSaldo();
    String s2 = null;
    if (k.getSaldoDatum() != null)
      s2 = HBCI.DECIMALFORMAT.format(d) + " " + k.getWaehrung(); // Saldo wurde schonmal abgerufen

    if (s2 == null)
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. {0}",s1));
    else
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. {0}, Saldo: {1}",new String[]{s1,s2}));

    ColumnLayout columns = new ColumnLayout(getParent(),2);
    SimpleContainer left = new SimpleContainer(columns.getComposite());

		// BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    left.addHeadline(i18n.tr("Gegenkonto"));
    left.addLabelPair(i18n.tr("Inhaber"),                       control.getEmpfaengerName());
    left.addLabelPair(i18n.tr("Kontonummer"),                   control.getEmpfaengerKonto());
    left.addLabelPair(i18n.tr("BLZ"),                           control.getEmpfaengerBLZ());

    left.addHeadline(i18n.tr("Datum und Betrag"));
    left.addLabelPair(i18n.tr("Betrag"),                        control.getBetrag());
    left.addLabelPair(i18n.tr("Datum der Buchung"),             control.getDatum());
    left.addLabelPair(i18n.tr("Valuta"),                        control.getValuta());
    left.addLabelPair(i18n.tr("Neuer Saldo"),                   control.getSaldo());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());

    right.addHeadline(i18n.tr("Sonstige Informationen"));
    right.addLabelPair(i18n.tr("Art der Buchung"),               control.getArt());
    right.addLabelPair(i18n.tr("Kundenreferenz"),                control.getCustomerRef());
    right.addLabelPair(i18n.tr("Primanota-Kennzeichen"),         control.getPrimanota());

    SimpleContainer bottom = new SimpleContainer(getParent(),true);
    bottom.addLabelPair(i18n.tr("Umsatz-Kategorie"),              control.getUmsatzTyp());
    bottom.addLabelPair(i18n.tr("Verwendungszweck"),              control.getZweck());

    ButtonArea buttons = new ButtonArea(getParent(),2);
		buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    });
  }
}


/**********************************************************************
 * $Log: UmsatzEdit.java,v $
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/