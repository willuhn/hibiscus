/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/SammelLastschriftDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/03/05 19:11:25 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt die uebergebene Sammel-Lastschrift an.
 * Wird verwendet, wenn eine Sammel-Lastschrift ausgefuehrt werden
 * soll - dann wird vorher diese Sicherheitsabfrage eingeblendet, die
 * nochmal die Details der Sammel-Lastschrift anzeigt. Erst wenn der User
 * hier OK klickt, wird die Lastschrift ausgefuehrt.
 */
public class SammelLastschriftDialog extends AbstractDialog {

	private I18N i18n;
	private SammelLastschrift ueb;
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param u Lastschrift.
   * @param position
   */
  public SammelLastschriftDialog(SammelLastschrift u, int position) {
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
		LabelGroup group = new LabelGroup(parent,i18n.tr("Details der Lastschrift"));
			
		group.addText(i18n.tr("Sind Sie sicher, daß Sie die Sammel-Lastschrift jetzt einreichen wollen?") + "\n",true);

		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Gutschriftskonto"),kto);

		group.addSeparator();

		group.addText(i18n.tr("Enthaltene Buchungen"),false);

		DBIterator list = ueb.getBuchungen();
		while (list.hasNext())
		{
			SammelLastBuchung b = (SammelLastBuchung) list.next();
			Input empfName = new LabelInput(b.getGegenkontoName());
			group.addLabelPair(i18n.tr("Names des Zahlungspflichtigen"),empfName);

			Input empfKto = new LabelInput(b.getGegenkontoNummer());
			empfKto.setComment(b.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(b.getGegenkontoBLZ()));
			group.addLabelPair(i18n.tr("Zu belastendes Konto"),empfKto);

			group.addSeparator();

			String s = b.getZweck();
			String s2 = b.getZweck2();
			if (s2 != null && s2.length() > 0)
				s += " / " + s2;
			Input zweck = new LabelInput(s);
			group.addLabelPair(i18n.tr("Verwendungszweck"),zweck);

			Input betrag = new LabelInput(HBCI.DECIMALFORMAT.format(b.getBetrag()) + " " + ueb.getKonto().getWaehrung());
			group.addLabelPair(i18n.tr("Betrag"),betrag);
			group.addSeparator();
		}

		ButtonArea b = group.createButtonArea(2);
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
 * $Log: SammelLastschriftDialog.java,v $
 * Revision 1.1  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/03/02 17:59:31  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/03/01 00:38:27  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/19 00:16:05  willuhn
 * @N Lastschriften
 *
 **********************************************************************/