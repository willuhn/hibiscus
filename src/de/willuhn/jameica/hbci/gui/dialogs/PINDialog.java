/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PINDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2004/07/25 17:15:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Dialog für die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog {

	private I18N i18n;
  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PINDialog(int position) {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    setTitle(i18n.tr("PIN-Eingabe"));
    setLabelText(i18n.tr("Ihre PIN"));
    setText(i18n.tr("Bitte geben Sie Ihre PIN ein."));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() != 5)
		{
			setErrorText(i18n.tr("Fehler: PIN muss fünfstellig sein.") + " " + getRetryString());
			return false;
		}

		if (!Settings.getCheckPin())
			return true;

		// PIN-Ueberpruefung aktiv. Also checken wir die Pruef-Summe
		String checkSum = Settings.getCheckSum();
		if (checkSum == null || checkSum.length() == 0)
		{
			// Das ist die erste Eingabe. Dann koennen wir nur
			// eine neue Check-Summe bilden, sie abspeichern und
			// hoffen, dass sie richtig eingegeben wurd.
			try {
				Settings.setCheckSum(createCheckSum(password));
			}
			catch (NoSuchAlgorithmException e)
			{
				Logger.error("hash algorithm not found",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Prüfsumme konnte nicht ermittelt werden. Option wurde deaktiviert."));
				Settings.setCheckPin(false);
				Settings.setCheckSum(null);
			}
			return true;
		}
		// Check-Summe existiert, dann vergleichen wir die Eingabe
		else {
			String n = null;
			try {
				n = createCheckSum(password);
			}
			catch (NoSuchAlgorithmException e)
			{
				Logger.error("hash algorithm not found",e);
				GUI.getStatusBar().setErrorText(i18n.tr("Prüfsumme konnte nicht verglichen werden. Option wurde deaktiviert."));
				Settings.setCheckPin(false);
				Settings.setCheckSum(null);
				return true;
			}
			if (n != null && checkSum != null && n.length() > 0 && checkSum.length() > 0 && n.equals(checkSum))
			{
				// Eingabe korrekt
				return true;
			}
    }
		setErrorText(i18n.tr("PIN falsch.") + " " + getRetryString());
		return false;
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

  /**
	 * Erzeugt eine Check-Summe aus dem uebergebenen String.
   * @param s der String.
   * @return erzeugte Check-Summe.
   * @throws NoSuchAlgorithmException Wenn die benoetigten Hash-Algorithmen nicht zur Verfuegung stehen.
   */
  private static String createCheckSum(String s) throws NoSuchAlgorithmException
	{
		// Es ist vielleicht etwas uebertrieben - ich weiss, aber ich
		// will auf Nummer sicher gehen. Deswegen mach ich aus dem String
		// erst einen MD5-Hash und bilde aus dem dann einen SHA1-Hash.
		// Hintergrund: Kriegt jemand die Datei mit dem Hash in die
		// Finger, wird er sich mit einer Woertbuch-Attacke lange versuchen,
		// da er wahrscheinlich nicht weiss, dass das Passwort doppelt
		// gehasht ist.
		MessageDigest md = null;
		byte[] hashed = null;
		md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(s.getBytes());
		md = MessageDigest.getInstance("SHA1");
		hashed = md.digest(b);

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(hashed);
	}
}


/**********************************************************************
 * $Log: PINDialog.java,v $
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/03/03 22:26:41  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/