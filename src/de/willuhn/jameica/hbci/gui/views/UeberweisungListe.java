/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UeberweisungListe.java,v $
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

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Ueberweisungen an.
 */
public class UeberweisungListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		GUI.setTitleText(I18N.tr("Vorhandene Überweisungen"));
		
		UeberweisungControl control = new UeberweisungControl(this);
		
		try {

			control.getUeberweisungListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addCreateButton(I18N.tr("neue Überweisung"),control);

		}
		catch (Exception e)
		{
			Application.getLog().error("error while loading ueberweisung list",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Überweisungen."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: UeberweisungListe.java,v $
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/