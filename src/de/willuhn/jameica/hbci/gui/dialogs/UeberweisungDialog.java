/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UeberweisungDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/26 23:23:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt die uebergebene Ueberweisung an.
 * Wird verwendet, wenn eine Ueberweisung ausgefuehrt werden
 * soll - dann word vorher eine Sicherheitsabfrage eingeblendet, die
 * nochmal die Details der Ueberweisung anzeigt. Erst wenn der User
 * hier OK klickt, wird die Ueberweisung ausgefuehrt.
 */
public class UeberweisungDialog extends AbstractDialog {

	private I18N i18n;
	private Ueberweisung ueb;
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param u Ueberweisung die anzuzeigende Ueberweisung.
   * @param position
   */
  public UeberweisungDialog(Ueberweisung u, int position) {
    super(position);

		i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

    this.ueb = u;
    this.setTitle(i18n.tr("Sicher?"));
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return choosen;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Details der Überweisung"));
			
		group.addText(i18n.tr("Sind Sie sicher, daß Sie die Überweisung jetzt ausführen wollen?") + "\n",true);

		AbstractInput kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		group.addSeparator();

		AbstractInput empfName = new LabelInput(ueb.getEmpfaengerName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		AbstractInput empfKto = new LabelInput(ueb.getEmpfaengerKonto());
		empfKto.setComment(ueb.getEmpfaengerBlz() + "/" + HBCIUtils.getNameForBLZ(ueb.getEmpfaengerBlz()));
		group.addLabelPair(i18n.tr("Konto des Empfängers"),empfKto);

		group.addSeparator();

		AbstractInput zweck = new LabelInput(ueb.getZweck() + "/" + ueb.getZweck2());
		group.addLabelPair(i18n.tr("Verwendungszweck"),zweck);

		AbstractInput betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
		group.addLabelPair(i18n.tr("Betrag"),betrag);

		ButtonArea b = group.createButtonArea(2);
		b.addCustomButton(i18n.tr("OK"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				choosen = Boolean.TRUE;
				close();
			}
		});
		b.addCustomButton(i18n.tr("Abbrechen"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				choosen = Boolean.FALSE;
				close();
			}
		});

  }

}


/**********************************************************************
 * $Log: UeberweisungDialog.java,v $
 * Revision 1.1  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 **********************************************************************/