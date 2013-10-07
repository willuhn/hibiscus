/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AuslandsUeberweisungDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/11 10:05:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class SepaLastschriftDialog extends AbstractExecuteDialog
{
	private SepaLastschrift last;

  /**
   * ct.
   * @param u die anzuzeigende Auslandsueberweisung.
   * @param position
   */
  public SepaLastschriftDialog(SepaLastschrift u, int position)
  {
    super(position);
    this.last = u;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addHeadline(i18n.tr("Details der SEPA-Überweisung"));
			
		Input kto = new LabelInput(last.getKonto().getKontonummer());
		kto.setComment(last.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		Input empfName = new LabelInput(last.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Zahlungspflichtigen"),empfName);

		Input empfKto = new LabelInput(last.getGegenkontoNummer());
		group.addLabelPair(i18n.tr("IBAN des Zahlungspflichtigen"),empfKto);

    Input empfBic = new LabelInput(last.getGegenkontoBLZ());
    group.addLabelPair(i18n.tr("BIC des Zahlungspflichtigen"),empfBic);

    Input empfMandate = new LabelInput(last.getMandateId());
    group.addLabelPair(i18n.tr("Mandats-ID"),empfMandate);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(last.getBetrag()) + " " + last.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(last,"\n"),false);
    
    group.addText(i18n.tr("Sind Sie sicher, daß Sie den Auftrag jetzt ausführen wollen?") + "\n",true);

    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }
}
