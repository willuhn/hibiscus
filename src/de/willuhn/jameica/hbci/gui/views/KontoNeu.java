/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoNeu.java,v $
 * $Revision: 1.15 $
 * $Date: 2004/06/30 20:58:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Bankverbindung bearbeiten.
 */
public class KontoNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Bankverbindung bearbeiten"));
		
		final KontoControl control = new KontoControl(this);

		try {

			LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

			group.addLabelPair(i18n.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(i18n.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(i18n.tr("Bezeichnung des Kontos"),		control.getBezeichnung());
			group.addLabelPair(i18n.tr("Kontoinhaber"),			    		control.getName());
			group.addLabelPair(i18n.tr("Kundennummer"),							control.getKundennummer());
      group.addLabelPair(i18n.tr("Währungsbezeichnung"),  		control.getWaehrung());
			group.addLabelPair(i18n.tr("Sicherheitsmedium"),    		control.getPassportAuswahl());

			// und noch die Abschicken-Knoepfe
			ButtonArea buttonArea = group.createButtonArea(3);
			buttonArea.addCancelButton(control);
			buttonArea.addDeleteButton(control);
			buttonArea.addStoreButton(control);


			LabelGroup saldo = new LabelGroup(getParent(),i18n.tr("Finanzstatus"));

			saldo.addLabelPair(i18n.tr("Saldo"),										control.getSaldo());
			saldo.addLabelPair(i18n.tr("letzte Aktualisierung"),		control.getSaldoDatum());

			ButtonArea buttons = saldo.createButtonArea(2);
			buttons.addCustomButton(i18n.tr("Saldo aktualisieren"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleRefreshSaldo();
				}
			});
			buttons.addCustomButton(i18n.tr("Kontoauszüge"), new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					control.handleShowUmsaetze();
				}
			});

			new Headline(getParent(),i18n.tr("Protokoll des Kontos"));
			control.getProtokoll().paint(getParent());

			control.init();
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading konto",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Bankverbindungsdaten."));
		}

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: KontoNeu.java,v $
 * Revision 1.15  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/05/26 23:23:10  willuhn
 * @N neue Sicherheitsabfrage vor Ueberweisung
 * @C Check des Ueberweisungslimit
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.13  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.12  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.11  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/05 00:04:10  willuhn
 * @N added code for umsatzlist
 *
 * Revision 1.8  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.6  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 15:40:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/