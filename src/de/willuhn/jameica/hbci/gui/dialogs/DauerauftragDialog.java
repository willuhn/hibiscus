/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/DauerauftragDialog.java,v $
 * $Revision: 1.6 $
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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Sicherheitsabfrage beim Ausfuehren eines Auftrages.
 */
public class DauerauftragDialog extends AbstractExecuteDialog
{
	private Dauerauftrag auftrag;

  /**
   * ct.
   * @param d anzuzeigender Dauerauftrag.
   * @param position
   */
  public DauerauftragDialog(Dauerauftrag d, int position)
  {
    super(position);
    this.auftrag = d;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);
		group.addHeadline(i18n.tr("Details des Dauerauftrages"));
			
		Input kto = new LabelInput(auftrag.getKonto().getKontonummer());
		kto.setComment(auftrag.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		Input empfName = new LabelInput(auftrag.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		Input empfKto = new LabelInput(auftrag.getGegenkontoNummer());
		empfKto.setComment(auftrag.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(auftrag.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Konto des Empfängers"),empfKto);

    LabelInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(auftrag.getBetrag()) + " " + auftrag.getKonto().getWaehrung());
    betrag.setColor(Color.ERROR);
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    group.addSeparator();

    Date e = auftrag.getErsteZahlung();
    String se = i18n.tr("Zum nächstmöglichen Termin");
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
      group.addText(i18n.tr("Sind Sie sicher, daß Sie diese Änderungen jetzt zur Bank senden wollen?") + "\n",true);
    else
      group.addText(i18n.tr("Sind Sie sicher, daß Sie diesen Dauerauftrag jetzt ausführen wollen?") + "\n",true);
    
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

}


/**********************************************************************
 * $Log: DauerauftragDialog.java,v $
 * Revision 1.6  2011/05/11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 **********************************************************************/