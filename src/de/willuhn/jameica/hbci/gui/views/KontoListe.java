/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/KontoListe.java,v $
 * $Revision: 1.4 $
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

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoListe extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		GUI.setTitleText(I18N.tr("Vorhandene Bankverbindungen"));
		
		final KontoControl control = new KontoControl(this);
		
		try {

			control.getKontoListe().paint(getParent());

			control.getPassportAuswahl().paint(getParent());
			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addCustomButton(I18N.tr("Konten aus Medium lesen"), new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
        	control.handleReadFromPassport();
        }
      });
			buttons.addCreateButton(I18N.tr("Neue Bankverbindung"),control);

		}
		catch (Exception e)
		{
			Application.getLog().error("error while loading konto list",e);
			GUI.setActionText(I18N.tr("Fehler beim Lesen der Bankverbindungen."));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: KontoListe.java,v $
 * Revision 1.4  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/