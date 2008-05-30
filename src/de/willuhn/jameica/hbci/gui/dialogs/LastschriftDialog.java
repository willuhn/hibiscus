/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/LastschriftDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/05/30 12:02:08 $
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt die uebergebene Lastschrift an.
 * Wird verwendet, wenn eine Lastschrift ausgefuehrt werden
 * soll - dann wird vorher diese Sicherheitsabfrage eingeblendet, die
 * nochmal die Details der Lastschrift anzeigt. Erst wenn der User
 * hier OK klickt, wird die Lastschrift ausgefuehrt.
 */
public class LastschriftDialog extends AbstractDialog {

	private I18N i18n;
	private Lastschrift ueb;
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param u Lastschrift.
   * @param position
   */
  public LastschriftDialog(Lastschrift u, int position) {
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
			
		group.addText(i18n.tr("Sind Sie sicher, daß Sie die Lastschrift jetzt einreichen wollen?") + "\n",true);

		Input kto = new LabelInput(ueb.getKonto().getKontonummer());
		kto.setComment(ueb.getKonto().getBezeichnung());
		group.addLabelPair(i18n.tr("Eigenes Konto (Empfänger)"),kto);

		group.addSeparator();

		Input empfName = new LabelInput(ueb.getGegenkontoName());
		group.addLabelPair(i18n.tr("Names des Zahlungspflichtigen"),empfName);

		Input empfKto = new LabelInput(ueb.getGegenkontoNummer());
		empfKto.setComment(ueb.getGegenkontoBLZ() + "/" + HBCIUtils.getNameForBLZ(ueb.getGegenkontoBLZ()));
		group.addLabelPair(i18n.tr("Zu belastendes Konto"),empfKto);

		group.addSeparator();

    Input betrag = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getBetrag()) + " " + ueb.getKonto().getWaehrung());
    group.addLabelPair(i18n.tr("Betrag"),betrag);

    // BUGZILLA 32 http://www.willuhn.de/bugzilla/show_bug.cgi?id=32
    LabelGroup zweck = new LabelGroup(parent,i18n.tr("Verwendungszweck"));
    zweck.addText(ueb.getZweck(),false);
    String z2 = ueb.getZweck2();
    if (z2 != null && z2.length() > 0)
      zweck.addText(z2,false);

    GenericIterator moreUsages = ueb.getWeitereVerwendungszwecke();
    while (moreUsages != null && moreUsages.hasNext())
    {
      Verwendungszweck z = (Verwendungszweck) moreUsages.next();
      String text = z.getText();
      if (text == null || text.length() == 0)
        continue;
      zweck.addText(text,false);
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
 * $Log: LastschriftDialog.java,v $
 * Revision 1.5  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.4  2005/04/05 22:49:02  web0
 * @B bug 32
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