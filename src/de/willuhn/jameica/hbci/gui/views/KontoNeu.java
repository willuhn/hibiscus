/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoNeu.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/02/27 01:10:18 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bankverbindung bearbeiten.
 */
public class KontoNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		GUI.setTitleText(I18N.tr("Bankverbindung bearbeiten"));
		
		final KontoControl control = new KontoControl(this);
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));

		LabelGroup saldo = new LabelGroup(getParent(),I18N.tr("Aktueller Saldo"));


		try {
			group.addLabelPair(I18N.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(I18N.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(I18N.tr("Kontoinhaber"),			    		control.getName());
			group.addLabelPair(I18N.tr("Kundennummer"),							control.getKundennummer());
      group.addLabelPair(I18N.tr("Währungsbezeichnung"),  		control.getWaehrung());
			group.addLabelPair(I18N.tr("Sicherheitsmedium"),    		control.getPassportAuswahl());

			saldo.addLabelPair(I18N.tr("Saldo"),										control.getSaldo());
			saldo.addLabelPair(I18N.tr("letzte Aktualisierung"),		control.getSaldoDatum());

			control.init();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while reading konto",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Bankverbindungsdaten."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addCustomButton(I18N.tr("Saldo aktualisieren"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleRefreshSaldo();
			}
		});
		buttonArea.addCancelButton(control);
		buttonArea.addDeleteButton(control);
		buttonArea.addStoreButton(control);


  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: KontoNeu.java,v $
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