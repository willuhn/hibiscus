/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class SepaSammelTransferDialog extends AbstractExecuteDialog
{
	private SepaSammelTransfer st;

  /**
   * ct.
   * @param s der Sammel-Auftrag.
   * @param position
   */
  public SepaSammelTransferDialog(SepaSammelTransfer s, int position)
  {
    super(position);
    this.st = s;
    
    // Wird sonst entweder zu flach oder zu schmal
    this.setSize(550,440);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent,false);
    group.addHeadline(i18n.tr("Details des SEPA-Sammelauftrages"));
			
    group.addLabelPair(i18n.tr("Bezeichnung"),new LabelInput(this.st.getBezeichnung()));

    Input kto = new LabelInput(st.getKonto().getIban());
    kto.setComment(st.getKonto().getBezeichnung());
    group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(st.getSumme()) + " " + st.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Summe"),betrag);

    group.addHeadline(i18n.tr("Enthaltene Buchungen"));
    TablePart buchungen = new SepaSammelTransferBuchungList(this.st,null);
    buchungen.setMulti(false);
    buchungen.setSummary(false);
    buchungen.paint(parent);

    super.paint(parent);
  
  }

}
