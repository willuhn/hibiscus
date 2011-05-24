/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/KeyPasswordSaveDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/24 09:06:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe des Passwortes beim Speichern eines Schluessels.
 */
public class KeyPasswordSaveDialog extends NewPasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
  /**
   * ct.
   * @param position Position des Dialogs.
   */
  public KeyPasswordSaveDialog(int position)
  {
    super(position);

    setTitle(i18n.tr("Passwort-Eingabe"));
    setLabelText(i18n.tr("Ihr Passwort"));
    setText(i18n.tr("Bitte vergeben Sie ein Passwort, mit dem der zu speichernde\nSchlüssel geschützt werden soll."));
  }
}


/**********************************************************************
 * $Log: KeyPasswordSaveDialog.java,v $
 * Revision 1.1  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.6  2010-11-22 11:30:51  willuhn
 * @C Cleanup
 *
 **********************************************************************/