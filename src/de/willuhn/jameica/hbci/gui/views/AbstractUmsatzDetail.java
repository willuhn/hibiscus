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

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Bildet die Detailansicht einer Buchung ab.
 */
public abstract class AbstractUmsatzDetail extends AbstractView
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Liefert den Controller.
   * @return der Controller.
   */
  protected abstract UmsatzDetailControl getControl();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

    final UmsatzDetailControl control = getControl();
    
    // BUGZILLA 38 http://www.willuhn.de/bugzilla/show_bug.cgi?id=38
    final Umsatz u = control.getUmsatz();
    Konto k = u.getKonto();

    String s1 = k.getLongName();
    if (s1 == null) s1 = "";

    double d = k.getSaldo();
    String s2 = null;
    if (k.getSaldoDatum() != null)
      s2 = HBCI.DECIMALFORMAT.format(d) + " " + k.getWaehrung(); // Saldo wurde schonmal abgerufen

    if (s2 == null)
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. {0}",s1));
    else
      GUI.getView().setTitle(i18n.tr("Buchungsdetails. {0}, Saldo: {1}", s1, s2));

    ColumnLayout columns = new ColumnLayout(getParent(),2);
    SimpleContainer left = new SimpleContainer(columns.getComposite());

		// BUGZILLA 23 http://www.willuhn.de/bugzilla/show_bug.cgi?id=23
    left.addHeadline(i18n.tr("Gegenkonto"));
    left.addLabelPair(i18n.tr("Name"),                       control.getEmpfaengerName());

    // Name 2 erstmal nur anzeigen, wenn was drin steht
    if (StringUtils.isNotBlank(control.getUmsatz().getGegenkontoName2()))
      left.addInput(control.getEmpfaengerName2());
    
    left.addInput(control.getEmpfaengerKonto());
    left.addInput(control.getEmpfaengerBLZ());

    left.addHeadline(i18n.tr("Datum und Betrag"));
    left.addLabelPair(i18n.tr("Datum"),                         control.getDatum());
    left.addLabelPair(i18n.tr("Wertstellung"),                  control.getValuta());
    left.addSeparator();
    left.addLabelPair(i18n.tr("Betrag"),                        control.getBetrag());
    left.addLabelPair(i18n.tr("Neuer Saldo"),                   control.getSaldo());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("Sonstige Informationen"));
    right.addLabelPair(i18n.tr("Art der Buchung"),              control.getArt());
    right.addInput(control.getEndToEndId());
    right.addInput(control.getCreditorId());
    right.addLabelPair(i18n.tr("Kunden-/Mandatsreferenz"),      new MultiInput(control.getCustomerRef(),control.getMandateId()));
    right.addLabelPair(i18n.tr("Primanota/GV-Code"),new MultiInput(control.getPrimanota(),control.getGvCode()));
    
    right.addHeadline(i18n.tr("Notizen"));
    right.addPart(control.getKommentar());

    SimpleContainer bottom = new SimpleContainer(getParent(),true);
    bottom.addSeparator();
    bottom.addLabelPair(i18n.tr("Kategorie"),                   control.getUmsatzTyp());
    bottom.addHeadline(i18n.tr("Verwendungszweck"));
    bottom.addPart(control.getZweck());
    bottom.addInput(control.getZweckSwitch());

    forceSaldoUpdateforReverseBooking();
    NeueUmsaetze.setRead(u);
  }

  private void forceSaldoUpdateforReverseBooking()
  {
    if(getCurrentObject() instanceof Umsatz){
      Umsatz umsatz=(Umsatz)getCurrentObject();
      try
      {
        if(umsatz.isNewObject() && umsatz.getKonto().hasFlag(Konto.FLAG_OFFLINE)){
          getControl().getBetrag().getControl().forceFocus();//->Saldenberechnungslistener feuert
          getControl().getUmsatzTyp().focus();
        }
      } catch (RemoteException e)
      {
        //einfach ignoriere, wir wollen ja nur ein Feld vorsorglich aktualisieren
      }
    }
  }
}
