/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SammelLastschriftNew.java,v $
 * $Revision: 1.13 $
 * $Date: 2006/08/07 14:31:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.action.SammelTransferDelete;
import de.willuhn.jameica.hbci.gui.controller.SammelLastschriftControl;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Sammel-Lastschriften.
 */
public class SammelLastschriftNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SammelLastschriftControl control = new SammelLastschriftControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Sammel-Lastschrift bearbeiten"));
		
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Gutschriftskonto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());
    group.addLabelPair(i18n.tr("Termin"),control.getTermin());
		
		group.addSeparator();
    group.addLabelPair(i18n.tr("Summe der Buchungen"),control.getSumme());
		group.addLabelPair("",control.getComment());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());

		final SammelLastschrift l = (SammelLastschrift) control.getTransfer();

    ButtonArea buttons = new ButtonArea(getParent(),5);
    buttons.addButton(i18n.tr("Zurück"),new Back());
    buttons.addButton(i18n.tr("Löschen"),new SammelTransferDelete(),control.getTransfer());
    buttons.addButton(i18n.tr("Neue Buchungen hinzufügen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore())
          new SammelLastBuchungNew().handleAction(l);
      }
    });
		buttons.addButton(i18n.tr("Speichern und ausführen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        if (control.handleStore())
  				new SammelLastschriftExecute().handleAction(l);
			}
		},null,true);
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,true);

  }
}


/**********************************************************************
 * $Log: SammelLastschriftNew.java,v $
 * Revision 1.13  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.12  2006/06/13 20:09:06  willuhn
 * @R Text "Bemerkung" entfernt
 *
 * Revision 1.11  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.10  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.9  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.8  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.7  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.6  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.5  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.4  2005/03/05 19:19:48  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/