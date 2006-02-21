/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.23 $
 * $Date: 2006/02/21 22:51:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.gui.dialogs.AccountContainerDialog;
import de.willuhn.jameica.hbci.gui.dialogs.NewInstKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.NewKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportLoadDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportSaveDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PtSechMechDialog;
import de.willuhn.jameica.hbci.gui.dialogs.TANDialog;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static AbstractDialog dialog = null;

  /**
	 * Erzeugt einen simplen Dialog mit einem OK-Button.
   * @param headline Ueberschrift des Dialogs.
   * @param text Text des Dialogs.
   * @throws Exception
   */
  public static synchronized void openSimple(final String headline, final String text) throws Exception
	{
		check();
		SimpleDialog d = new SimpleDialog(AbstractDialog.POSITION_CENTER);
		d.setTitle(headline);
		d.setText(text);
		dialog = d;
		try
		{
			d.open();
		}
		finally
		{
			close();
		}
	}

  /**
	 * Erzeugt den PIN-Dialog.
	 * Hinweis: Wirft eine RuntimeException, wenn der PIN-Dialog abgebrochen
	 * oder die PIN drei mal falsch eingegeben wurde (bei aktivierter Checksummen-Pruefung).
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
   * @param passport Passport, fuer den die PIN-Abfrage gemacht wird. Grund: Der
   * PIN-Dialog hat eine eingebaute Checksummen-Pruefung um zu checken, ob die
   * PIN richtig eingegeben wurde. Da diese Checksumme aber pro Passport gespeichert
   * wird, benoetigt der Dialoig eben jenen.
	 * @return die eingegebene PIN.
   * @throws Exception
	 */
	public static synchronized String getPIN(HBCIPassport passport) throws Exception
	{
		check();
		dialog = new PINDialog(passport);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Laden eines zu importierenden Passports.
   * @return eingegebenes Passwort.
   * @throws Exception
   */
  public static synchronized String importPassport() throws Exception
	{
		check();
		dialog = new PassportLoadDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Speichern eines zu exportierenden Passports.
	 * @return eingegebenes Passwort.
	 * @throws Exception
	 */
	public static synchronized String exportPassport() throws Exception
	{
		check();
		dialog = new PassportSaveDialog(AbstractDialog.POSITION_CENTER);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

  /**
	 * Erzeugt einen TAN-Dialog.
	 * Hinweis: Wirft eine RuntimeException, wenn der TAN-Dialog abgebrochen wurde.
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
   * @param text anzuzeigender Text.
	 * @return die eingegebene TAN.
   * @throws Exception
	 */
	public static synchronized String getTAN(String text) throws Exception
	{
		check();
		dialog = new TANDialog();
    ((TANDialog)dialog).setText(text);
		try {
			return (String) dialog.open();
		}
		finally
		{
			close();
		}
	}

  /**
   * BUGZILLA 200
   * Erzeugt einen Dialog zur Abfrage der PIN/TAN-Scurity-Methode.
   * @param options die zur Verfuegung stehenden Optionen.
   * @return die ID der Methode.
   * @throws Exception
   */
  public static synchronized String getPtSechMech(String options) throws Exception
  {
    check();
    dialog = new PtSechMechDialog(options);
    try {
      return (String) dialog.open();
    }
    finally
    {
      close();
    }
  }

  /**
	 * Erzeugt einen Dialog zur Eingabe von Account-Daten.
	 * Hinweis: Wirft eine RuntimeException, wenn der Dialog abgebrochen wurde.
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
	 * @param p der Passport.
   * @return ein Container mit den eingegebenen Daten.
   * @throws Exception
   */
  public static synchronized AccountContainer getAccountData(HBCIPassport p) throws Exception
	{
		check();
		dialog = new AccountContainerDialog(p);
		try {
			return (AccountContainer) dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Erzeugt einen Dialog Verifizierung der uebertragenen Instituts-Schluessel.
	 * Hinweis: Wirft eine RuntimeException, wenn der Dialog abgebrochen wurde.
	 * Hintergrund: Der Dialog wurde aus dem HBCICallBack heraus aufgerufen und soll im
	 * Fehlerfall den HBCI-Vorgang abbrechen.
	 * @param p der Passport.
	 * @return Entscheidung, ob die Bank-Schluessel ok sind.
	 * @throws Exception
	 */
	public static synchronized String getNewInstKeys(HBCIPassport p) throws Exception
	{
		check();
		dialog = new NewInstKeysDialog(p);
		try {
			Boolean b = (Boolean) dialog.open();
			return b.booleanValue() ? "" : "ERROR";
		}
		finally
		{
			close();
		}
	}

  /**
	 * Erzeugt einen Dialog, der den neu erzeugten Schluessel anzeigt und den Benutzer
	 * auffordert, den Ini-Brief an seine Bank zu senden.
   * @param p Passport, fuer den neue Schluessel erzeugt wurden.
	 * @throws Exception
	 */
	public static synchronized void newKeys(HBCIPassport p) throws Exception
	{
		check();
		dialog = new NewKeysDialog(p);
		try {
			dialog.open();
		}
		finally
		{
			close();
		}
	}

	/**
   * Prueft, ob der Dialog geoeffnet werden kann.
   */
  private static synchronized void check()
	{
		if (dialog == null)
			return;

		Logger.error("alert: there's another opened dialog");
		throw new RuntimeException("alert: there's another opened dialog");
	}

	/**
   * Schliesst den gerade offenen Dialog.
   */
  public static synchronized void close()
	{
		if (dialog == null)
			return;
		try {
			dialog.close();
		}
		finally
		{
			dialog = null;
		}
	}
	
}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.23  2006/02/21 22:51:36  willuhn
 * @B bug 200
 *
 * Revision 1.22  2006/02/06 15:40:44  willuhn
 * @B bug 150
 *
 * Revision 1.21  2005/02/07 22:06:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.19  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 * Revision 1.13  2004/05/04 23:58:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/05/04 23:07:24  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.11  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.10  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.7  2004/02/24 22:47:05  willuhn
 * @N GUI refactoring
 *
 * Revision 1.6  2004/02/22 20:04:54  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.4  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/20 01:25:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 **********************************************************************/