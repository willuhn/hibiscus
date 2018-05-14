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
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class LastschriftDialog extends AbstractExecuteDialog
{
	private Lastschrift ueb;

  /**
   * ct.
   * @param u Lastschrift.
   * @param position
   */
  public LastschriftDialog(Lastschrift u, int position)
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
		group.addHeadline(i18n.tr("Details der Lastschrift"));
			
		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto (Empfänger)"),kto);

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Names des Zahlungspflichtigen"),empfName);

		Input empfKto = new LabelInput(ueb.getGegenkontoNummer());
		empfKto.setComment(ueb.getGegenkontoBLZ() + "/" + HBCIProperties.getNameForBank(ueb.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Zu belastendes Konto"),empfKto);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(ueb,"\n"),false);
    
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

}


/**********************************************************************
 * $Log: LastschriftDialog.java,v $
 * Revision 1.7  2011/05/11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 **********************************************************************/