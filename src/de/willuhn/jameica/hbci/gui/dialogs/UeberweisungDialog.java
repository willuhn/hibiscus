/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UeberweisungDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/07/21 23:54:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.plugin.PluginLoader;
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

		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

		group.addSeparator();

		Input empfName = new LabelInput(ueb.getEmpfaengerName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		Input empfKto = new LabelInput(ueb.getEmpfaengerKonto());
		empfKto.setComment(ueb.getEmpfaengerBLZ() + "/" + HBCIUtils.getNameForBLZ(ueb.getEmpfaengerBLZ()));
		group.addLabelPair(i18n.tr("Konto des Empfängers"),empfKto);

		group.addSeparator();

		Input zweck = new LabelInput(ueb.getZweck() + "/" + ueb.getZweck2());
		group.addLabelPair(i18n.tr("Verwendungszweck"),zweck);

		Input betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
		group.addLabelPair(i18n.tr("Betrag"),betrag);

		ButtonArea b = group.createButtonArea(2);
		b.addCustomButton(i18n.tr("OK"), new Listener()
    {
      public void handleEvent(Event event)
      {
				choosen = Boolean.TRUE;
				close();
      }
    });
		b.addCustomButton(i18n.tr("Abbrechen"), new Listener()
    {
      public void handleEvent(Event event)
      {
				choosen = Boolean.FALSE;
				close();
      }
    });
  }

}


/**********************************************************************
 * $Log: UeberweisungDialog.java,v $
 * Revision 1.5  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/20 22:53:03  willuhn
 * @C Refactoring
 *
 * Revision 1.3  2004/07/14 23:48:31  willuhn
 * @N mehr Code fuer Dauerauftraege
 *
 * Revision 1.2  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.1  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 **********************************************************************/