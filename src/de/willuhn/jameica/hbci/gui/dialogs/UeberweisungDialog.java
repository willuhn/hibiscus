/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UeberweisungDialog.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/06/24 07:55:41 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class UeberweisungDialog extends AbstractExecuteDialog
{
	private Ueberweisung ueb = null;

  /**
   * ct.
   * @param u Ueberweisung die anzuzeigende Ueberweisung.
   * @param position
   */
  public UeberweisungDialog(Ueberweisung u, int position)
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
		group.addHeadline(i18n.tr("Details der Überweisung"));
			
		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		Input empfKto = new LabelInput(ueb.getGegenkontoNummer());
		empfKto.setComment(ueb.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(ueb.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Konto des Empfängers"),empfKto);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    if (ueb.isTerminUeberweisung())
    {
      Input termin = new LabelInput(HBCI.DATEFORMAT.format(ueb.getTermin()));
      group.addLabelPair(i18n.tr("Fällig am"),termin);
    }

    group.addHeadline(i18n.tr("Verwendungszweck"));
    group.addText(VerwendungszweckUtil.toString(ueb,"\n"),false);

    group.addText(i18n.tr("Sind Sie sicher, daß Sie den Auftrag jetzt ausführen wollen?") + "\n",true);

    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

}


/**********************************************************************
 * $Log: UeberweisungDialog.java,v $
 * Revision 1.18  2011/06/24 07:55:41  willuhn
 * @C Bei Hibiscus-verwalteten Terminen besser "Fällig am" verwenden - ist nicht so missverstaendlich - der User denkt sonst ggf. es sei ein bankseitig terminierter Auftrag
 *
 * Revision 1.17  2011-05-11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 * Revision 1.16  2010-10-05 21:57:21  willuhn
 * *** empty log message ***
 **********************************************************************/