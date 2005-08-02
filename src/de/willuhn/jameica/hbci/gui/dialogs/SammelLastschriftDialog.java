/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/SammelLastschriftDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/08/02 20:09:33 $
 * $Author: web0 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.SammelLastBuchungList;
import de.willuhn.jameica.hbci.rmi.Konto;
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
    this.setSize(SWT.DEFAULT,380);
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

    LabelGroup group = new LabelGroup(parent,"");
			
		group.addText(i18n.tr("Sind Sie sicher, daß Sie die Sammel-Lastschrift jetzt einreichen wollen?"),true);

    group.addLabelPair(i18n.tr("Bezeichnung"),new LabelInput(ueb.getBezeichnung()));

    // BUGZILLA 106 http://www.willuhn.de/bugzilla/show_bug.cgi?id=106
    Konto k = ueb.getKonto();
    
    Input kto = new LabelInput(ueb.getKonto().getKontonummer());
    String com = k.getBezeichnung();
    String bank = HBCIUtils.getNameForBLZ(k.getBLZ());
    if (bank != null && bank.length() > 0)
      com += " [" + bank + "]";
    
		kto.setComment(com);
		group.addLabelPair(i18n.tr("Gutschriftskonto"),kto);

    Input s = new LabelInput(HBCI.DECIMALFORMAT.format(ueb.getSumme()));
    s.setComment(k.getWaehrung());
    group.addLabelPair(i18n.tr("Summe"),s);

    new Headline(parent,i18n.tr("Enthaltene Buchungen"));
    
    TablePart buchungen = new SammelLastBuchungList(ueb,null);
    buchungen.setMulti(false);
    buchungen.setSummary(false);

    buchungen.paint(parent);

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
 * $Log: SammelLastschriftDialog.java,v $
 * Revision 1.5  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.4  2005/04/05 22:49:02  web0
 * @B bug 32
 *
 * Revision 1.3  2005/03/06 14:04:26  web0
 * @N SammelLastschrift seems to work now
 *
 * Revision 1.2  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
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