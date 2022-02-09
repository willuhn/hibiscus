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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class SepaDauerauftragDialog extends AbstractExecuteDialog
{
	private SepaDauerauftrag auftrag;

  /**
   * ct.
   * @param d anzuzeigender SEPA-Dauerauftrag.
   * @param position
   */
  public SepaDauerauftragDialog(SepaDauerauftrag d, int position)
  {
    super(position);
    this.auftrag = d;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);
		group.addHeadline(i18n.tr("Details des SEPA-Dauerauftrages"));
			
    Input kto = new LabelInput(auftrag.getKonto().getKontonummer());
    kto.setComment(auftrag.getKonto().getBezeichnung());
    group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

    Input empfName = new LabelInput(auftrag.getGegenkontoName());
    group.addLabelPair(i18n.tr("Name des Empf�nger"),empfName);

    Input empfKto = new LabelInput(HBCIProperties.formatIban(auftrag.getGegenkontoNummer()));
    group.addLabelPair(i18n.tr("IBAN des Empf�ngers"),empfKto);

    Input empfBic = new LabelInput(auftrag.getGegenkontoBLZ());
    group.addLabelPair(i18n.tr("BIC des Empf�ngers"),empfBic);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(auftrag.getBetrag()) + " " + auftrag.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addSeparator();

    Date e = auftrag.getErsteZahlung();
    String se = i18n.tr("Zum n�chstm�glichen Termin");
    if (e != null) se = HBCI.DATEFORMAT.format(e);
    Input ersteZahlung = new LabelInput(se);
    group.addLabelPair(i18n.tr("Erste Zahlung"),ersteZahlung);

    Date l = auftrag.getLetzteZahlung();
    String sl = i18n.tr("keine End-Datum vorgegeben");
    if (l != null) sl = HBCI.DATEFORMAT.format(l);
    Input letzteZahlung = new LabelInput(sl);
    group.addLabelPair(i18n.tr("Letzte Zahlung"),letzteZahlung);

    Input turnus = new LabelInput(TurnusHelper.createBezeichnung(auftrag.getTurnus()));
    group.addLabelPair(i18n.tr("Zahlungsturnus"),turnus);

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(auftrag,"\n"),false);

    group.addSeparator();
    
    if (auftrag.isActive())
      group.addText(i18n.tr("Sind Sie sicher, da� Sie diese �nderungen jetzt zur Bank senden wollen?") + "\n",true);
    else
      group.addText(i18n.tr("Sind Sie sicher, da� Sie diesen Dauerauftrag jetzt ausf�hren wollen?") + "\n",true);
    
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

}
