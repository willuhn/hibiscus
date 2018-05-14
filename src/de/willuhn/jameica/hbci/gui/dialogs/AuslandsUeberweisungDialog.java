/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class AuslandsUeberweisungDialog extends AbstractExecuteDialog
{
	private AuslandsUeberweisung ueb;

  /**
   * ct.
   * @param u die anzuzeigende Auslandsueberweisung.
   * @param position
   */
  public AuslandsUeberweisungDialog(AuslandsUeberweisung u, int position)
  {
    super(position);
    this.ueb = u;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addHeadline(i18n.tr("Details der SEPA-Überweisung"));
			
		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		Input empfKto = new LabelInput(HBCIProperties.formatIban(ueb.getGegenkontoNummer()));
		group.addLabelPair(i18n.tr("IBAN des Empfängers"),empfKto);

    Input empfBic = new LabelInput(ueb.getGegenkontoBLZ());
    group.addLabelPair(i18n.tr("BIC des Empfängers"),empfBic);


    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    if (ueb.isTerminUeberweisung())
    {
      Input termin = new LabelInput(HBCI.DATEFORMAT.format(ueb.getTermin()));
      group.addLabelPair(i18n.tr("Ausführungstermin"),termin);
    }

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(ueb,"\n"),false);
    
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }
}
