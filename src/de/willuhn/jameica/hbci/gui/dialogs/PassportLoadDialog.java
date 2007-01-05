/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PassportLoadDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/01/05 17:23:24 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe eines Passwortes beim Laden des Passports.
 */
public class PassportLoadDialog extends PasswordDialog {

	private I18N i18n;
  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PassportLoadDialog(int position) {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    setTitle(i18n.tr("Passwort-Eingabe"));
    setLabelText(i18n.tr("Ihr Passwort"));
    setText(i18n.tr("Bitte geben Sie das von Ihnen vergebene Passwort\nfür dieses Sicherheitsmedium ein."));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Fehler: Bitte geben Sie Ihr Passwort ein.") + " " + getRetryString());
			return false;
		}
		return true;
	}

	/**
	 * Liefert einen locale String mit der Anzahl der Restversuche.
	 * z.Bsp.: "Noch 2 Versuche.".
   * @return String mit den Restversuchen.
   */
  private String getRetryString()
	{
		String retries = getRemainingRetries() > 1 ? i18n.tr("Versuche") : i18n.tr("Versuch");
		return (i18n.tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
	}
}


/**********************************************************************
 * $Log: PassportLoadDialog.java,v $
 * Revision 1.6  2007/01/05 17:23:24  jost
 * Zeilenumbruch korrigiert.
 *
 * Revision 1.5  2006/12/24 10:28:06  jost
 * Korrektur Tippfehler
 *
 * Revision 1.4  2005/02/07 22:06:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/02/06 19:03:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/