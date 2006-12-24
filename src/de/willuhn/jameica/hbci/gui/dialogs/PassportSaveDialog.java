/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PassportSaveDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/12/24 10:28:16 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe eines Passwortes beim Export des Passports.
 */
public class PassportSaveDialog extends NewPasswordDialog {

	private I18N i18n;
  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PassportSaveDialog(int position) {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    setTitle(i18n.tr("Passwort-Eingabe"));
    setLabelText(i18n.tr("Ihr Passwort"));
    setText(i18n.tr("Bitte vergeben Sie ein Passwort, mit dem der zu speichernde\nSchlüssel geschützt werden soll."));
  }
}


/**********************************************************************
 * $Log: PassportSaveDialog.java,v $
 * Revision 1.5  2006/12/24 10:28:16  jost
 * Korrektur Tippfehler
 *
 * Revision 1.4  2005/11/14 11:36:58  willuhn
 * @B bug 148
 *
 * Revision 1.3  2005/02/07 22:06:40  willuhn
 * *** empty log message ***
 *
 **********************************************************************/