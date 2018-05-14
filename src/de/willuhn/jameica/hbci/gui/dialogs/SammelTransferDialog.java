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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class SammelTransferDialog extends AbstractExecuteDialog
{
	private SammelTransfer st;

  /**
   * ct.
   * @param s der Sammel-Auftrag.
   * @param position
   */
  public SammelTransferDialog(SammelTransfer s, int position)
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
    group.addHeadline(i18n.tr("Details des Sammel-Auftrages"));
			
    group.addLabelPair(i18n.tr("Bezeichnung"),new LabelInput(this.st.getBezeichnung()));

    Input kto = new LabelInput(st.getKonto().getKontonummer());
    kto.setComment(st.getKonto().getBezeichnung());
    group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(st.getSumme()) + " " + st.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Summe"),betrag);

    group.addHeadline(i18n.tr("Enthaltene Buchungen"));
    TablePart buchungen = new SammelTransferBuchungList(this.st,null);
    buchungen.setMulti(false);
    buchungen.setSummary(false);
    buchungen.paint(parent);

    super.paint(parent);
  
  }

}


/**********************************************************************
 * $Log: SammelTransferDialog.java,v $
 * Revision 1.2  2011/05/11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/