/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoNeu.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/11 00:11:20 $
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
import org.eclipse.swt.widgets.Composite;

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
   * ct.
   * @param parent
   */
  public KontoNeu(Composite parent) {
    super(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		addHeadline("Bankverbindung bearbeiten");
		
		final KontoControl control = new KontoControl(this);
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));

		try {
			group.addLabelPair(I18N.tr("Kontonummer"),			control.getKontonummer());
			group.addLabelPair(I18N.tr("Bankleitzahl"),			control.getBlz());
			group.addLabelPair(I18N.tr("Kontoinhaber"),			control.getName());
			group.addLabelPair(I18N.tr("Sicherheitsmedium"),control.getPassport());

		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while reading konto",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Bankverbindungsdaten."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		buttonArea.addCustomButton(I18N.tr("Sicherheitsmedium konfigurieren"), new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				control.handleConfigurePassport();
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
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/