/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PINDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/02/22 20:04:53 $
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
import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.util.I18N;

/**
 * Dialog für die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog {

  /**
   * ct.
   * @param position Position des Dialogs.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_CENTER
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#POSITION_MOUSE
   */
  public PINDialog(int position) {
    super(position);
    setTitle(I18N.tr("PIN-Eingabe"));
    setLabelText(I18N.tr("Ihre PIN"));
    setText(I18N.tr("Bitte geben Sie Ihre PIN ein."));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() != 5)
		{
			setErrorText(I18N.tr("Fehler: PIN muss fünfstellig sein.") + " " + getRetryString());
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
				Application.getLog().error("hash algorithm not found",e);
				GUI.setActionText(I18N.tr("Prüfsumme konnte nicht ermittelt werden. Option wurde deaktiviert."));
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
				Application.getLog().error("hash algorithm not found",e);
				GUI.setActionText(I18N.tr("Prüfsumme konnte nicht verglichen werden. Option wurde deaktiviert."));
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
		setErrorText(I18N.tr("PIN falsch.") + " " + getRetryString());
		return false;
	}

	/**
	 * Liefert einen locale String mit der Anzahl der Restversuche.
	 * z.Bsp.: "Noch 2 Versuche.".
   * @return String mit den Restversuchen.
   */
  private String getRetryString()
	{
		String retries = getRemainingRetries() > 1 ? I18N.tr("Versuche") : I18N.tr("Versuch");
		return (I18N.tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
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
 * Revision 1.2  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.1  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 **********************************************************************/