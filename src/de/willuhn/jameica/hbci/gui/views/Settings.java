/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Settings.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/21 19:49:04 $
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
import de.willuhn.jameica.hbci.gui.controller.SettingsControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Einstellungs-Dialog.
 */
public class Settings extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {

		final SettingsControl control = new SettingsControl(this);
		
		LabelGroup settings = new LabelGroup(getParent(),I18N.tr("Einstellungen"));

		try {
			settings.addCheckbox(control.getOnlineMode(),I18N.tr("Keine Nachfrage vor Verbindungsaufbau"));
			settings.addCheckbox(control.getCheckPin(),I18N.tr("PIN-Eingabe via Check-Summe prüfen"));

			LabelGroup comments = new LabelGroup(getParent(),I18N.tr("Hinweise"));
			comments.addText(
				I18N.tr("Wenn Sie über eine dauerhafte Internetverbindung verfügen," +					"können Sie die Option \"keine Nachfrage vor Verbindungsaufbau " +					"aktivieren."),
				true
			);
			comments.addText(
				I18N.tr("Bei aktivierter PIN-Prüfung wird aus der von Ihnen eingegebene PIN " +					"eine Check-Summe gebildet und diese mit der Check-Summe Ihrer ersten PIN-Eingabe " +					"verglichen. Hierbei wird nicht die PIN selbst gespeichert sondern lediglich die " +					"Prüfsumme mit der ermittelt werden kann, ob Ihre aktuelle " +					"Eingabe mit der Erst-Eingabe übereinstimmt."),
				true
			);

			ButtonArea buttons = new ButtonArea(getParent(),3);
			buttons.addCancelButton(control);
			buttons.addStoreButton(control);
			buttons.addCustomButton(I18N.tr("gespeicherte Check-Summe löschen"),new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
					control.handleDeleteCheckSum();
        }
      });
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while showing settings",e);
			GUI.setActionText(I18N.tr("Fehler beim Laden der Einstellungen"));
		}
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
    // TODO Auto-generated method stub

  }

}


/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.3  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/