/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/SammelTransferDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:51 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog und zeigt den uebergebenen Sammel-Auftrag an.
 * Wird verwendet, wenn ein Sammel-Auftrag ausgefuehrt werden
 * soll - dann wird vorher diese Sicherheitsabfrage eingeblendet, die
 * nochmal die Details des Sammel-Auftrages anzeigt. Erst wenn der User
 * hier OK klickt, wird der Auftrag ausgefuehrt.
 */
public class SammelTransferDialog extends AbstractDialog {

	private I18N i18n;
	private SammelTransfer st;
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param s der Sammel-Auftrag.
   * @param position
   */
  public SammelTransferDialog(SammelTransfer s, int position) {
    super(position);

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.st = s;
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
			
		group.addText(i18n.tr("Sind Sie sicher, daß Sie den Sammel-Auftrag jetzt einreichen wollen?"),true);

    group.addLabelPair(i18n.tr("Bezeichnung"),new LabelInput(this.st.getBezeichnung()));

    // BUGZILLA 106 http://www.willuhn.de/bugzilla/show_bug.cgi?id=106
    Konto k = this.st.getKonto();
    
    Input kto = new LabelInput(this.st.getKonto().getKontonummer());
    String com = k.getBezeichnung();
    String bank = HBCIUtils.getNameForBLZ(k.getBLZ());
    if (bank != null && bank.length() > 0)
      com += " [" + bank + "]";
    
		kto.setComment(com);
		group.addLabelPair(i18n.tr("Eigenes Konto"),kto);

    Input s = new LabelInput(HBCI.DECIMALFORMAT.format(this.st.getSumme()));
    s.setComment(k.getWaehrung());
    group.addLabelPair(i18n.tr("Summe"),s);

    new Headline(parent,i18n.tr("Enthaltene Buchungen"));
    
    TablePart buchungen = new SammelTransferBuchungList(this.st,null);
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
 * $Log: SammelTransferDialog.java,v $
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 **********************************************************************/