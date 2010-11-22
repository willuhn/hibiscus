/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PassportSaveDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2010/11/22 11:30:51 $
 * $Author: willuhn $
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
public class PassportSaveDialog extends NewPasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PassportSaveDialog(int position)
  {
    super(position);

    setTitle(i18n.tr("Passwort-Eingabe"));
    setLabelText(i18n.tr("Ihr Passwort"));
    setText(i18n.tr("Bitte vergeben Sie ein Passwort, mit dem der zu speichernde\nSchlüssel geschützt werden soll."));
  }
}


/**********************************************************************
 * $Log: PassportSaveDialog.java,v $
 * Revision 1.6  2010/11/22 11:30:51  willuhn
 * @C Cleanup
 *
 **********************************************************************/