/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/EmpfaengerNeu.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/22 20:04:53 $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.EmpfaengerControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Empfaenger bearbeiten.
 */
public class EmpfaengerNeu extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
		
		GUI.setTitleText(I18N.tr("Empfänger bearbeiten"));
		
		final EmpfaengerControl control = new EmpfaengerControl(this);
		LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));

		try {
			group.addLabelPair(I18N.tr("Kontonummer"),			    		control.getKontonummer());
			group.addLabelPair(I18N.tr("Bankleitzahl"),			    		control.getBlz());
			group.addLabelPair(I18N.tr("Name"),			    						control.getName());

			control.init();
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while reading konto",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Empfängerdaten."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),3);
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
 * $Log: EmpfaengerNeu.java,v $
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/