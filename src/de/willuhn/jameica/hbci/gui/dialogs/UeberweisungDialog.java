/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UeberweisungDialog.java,v $
 * $Revision: 1.12 $
 * $Date: 2005/04/05 22:49:02 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt die uebergebene Ueberweisung an.
 * Wird verwendet, wenn eine Ueberweisung ausgefuehrt werden
 * soll - dann wird vorher diese Sicherheitsabfrage eingeblendet, die
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

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Name des Empfänger"),empfName);

		Input empfKto = new LabelInput(ueb.getGegenkontoNummer());
		empfKto.setComment(ueb.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(ueb.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Konto des Empfängers"),empfKto);

    group.addSeparator();

    Input betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    // BUGZILLA 32 http://www.willuhn.de/bugzilla/show_bug.cgi?id=32
    LabelGroup zweck = new LabelGroup(parent,i18n.tr("Verwendungszweck"));
    zweck.addText(ueb.getZweck(),true);
    String z2 = ueb.getZweck2();
    if (z2 != null && z2.length() > 0)
    {
      zweck.addSeparator();
      zweck.addText(z2,true);
    }


		ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Jetzt ausführen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				choosen = Boolean.TRUE;
				close();
      }
    });
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				choosen = Boolean.FALSE;
				close();
      }
    });
  }

}


/**********************************************************************
 * $Log: UeberweisungDialog.java,v $
 * Revision 1.12  2005/04/05 22:49:02  web0
 * @B bug 32
 *
 * Revision 1.11  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.10  2005/03/01 00:38:27  web0
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.7  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
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