/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.35 $
 * $Date: 2009/08/10 10:22:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import org.kapott.hbci.passport.AbstractRDHSWFileBasedPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportDDV;
import org.kapott.hbci.passport.HBCIPassportPinTan;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.AccountContainerDialog;
import de.willuhn.jameica.hbci.gui.dialogs.InternetConnectionDialog;
import de.willuhn.jameica.hbci.gui.dialogs.NewInstKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.NewKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportLoadDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PassportSaveDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory {

	private static AbstractDialog dialog = null;

  // BUGZILLA 185
  private static Hashtable pinCache = new Hashtable();

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
   * Zeigt einen Hinweis-Dialog an, der den User bittet, eine Internet-Verbindung herzustellen.
   * @throws Exception
   */
  public static synchronized void getConnection() throws Exception
  {
    if (Settings.getOnlineMode())
      return;

    check();
    dialog = new InternetConnectionDialog(AbstractDialog.POSITION_CENTER);
    try {
      dialog.open();
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

    boolean secondTry = System.currentTimeMillis() - lastTry < 400l;

    if (secondTry) Logger.warn("cached pin seems to be wrong, asking user, passport: " + passport.getClass().getName());

    String pin = getCachedPIN(passport);
    if (pin != null && !secondTry)
    {
      Logger.info("using cached pin, passport: " + passport.getClass().getName());
      lastTry = System.currentTimeMillis();
      return pin;
    }
    lastTry = 0;

    dialog = new PINDialog(passport);
		try {
			pin = (String) dialog.open();
		}
		finally
		{
			close();
		}
    setCachedPIN(passport,pin);
    return pin;
	}

  // Diese Variable soll davor schuetzen, dass Hibiscus faelschlicherweise der
  // Meinung ist, es wuerde das Passwort kennen, tut es aber gar nicht.
  // Das kann z.Bsp. passieren, wenn der User zwei Accounts mit der gleichen BLZ,
  // der gleichen Kundennummer aber verschiedenen Sicherheitsmedien hat. Sollte
  // eigentlich nur in Testszenarien vorkommen, aber man weiss ja nie ;)
  // Und da uns HBCI4Java schliesslich 3x nach dem Passwort fragt, wenn wir es falsch
  // liefern, koenne wir auch pruefen, ob wir innerhalb der letzten halben Sekunde
  // schonmal der Meinung waren, wir haetten eine Antwort gehabt. Ist das der Fall,
  // dann soll beim naechsten Mal der User entscheiden.
  private static long lastTry = 0;
  
	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Laden eines zu importierenden Passports.
   * @param passport der HBCI-Passport.
   * @return eingegebenes Passwort.
   * @throws Exception
   */
  public static synchronized String importPassport(HBCIPassport passport) throws Exception
	{
		check();
    
    HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
    boolean forceAsk = plugin.getResources().getSettings().getBoolean("hbcicallback.askpassphrase.force",false);

    // BUGZILLA 185: Da Schluesseldisketten keine PIN
    // haben, speichern wir hier das Passport der Datei
    // fuer die Session zwischen
    String pw = null;

    boolean secondTry = System.currentTimeMillis() - lastTry < 200l;
    
    if (secondTry) Logger.warn("cached key seems to be wrong, asking user, passport: " + passport.getClass().getName());
    
    if (!forceAsk && !secondTry)
    {
      pw = getCachedPIN(passport);
      if (pw != null)
      {
        Logger.info("using cached passport load key, passport: " + passport.getClass().getName());
        lastTry = System.currentTimeMillis();
        return pw; // wir haben ein gecachtes Passwort, das nehmen wir
      }
    }
    lastTry = 0;

    // Wir haben kein Passwort gecached oder
    // die Option ist deaktiviert. Also fragen
    // wir den User.
    Logger.info("ask user for passport load key, passport: " + passport.getClass().getName());
    dialog = new PassportLoadDialog(passport);
    try {
      pw = (String) dialog.open();
    }
    finally
    {
      close();
    }
    setCachedPIN(passport,pw);
    return pw;
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer das Sicherheitsmedium beim Speichern eines zu exportierenden Passports.
   * @param passport der Passport.
	 * @return eingegebenes Passwort.
	 * @throws Exception
	 */
	public static synchronized String exportPassport(HBCIPassport passport) throws Exception
	{
		check();
    Logger.info("ask user for passport save key, passport: " + passport.getClass().getName());
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

  /**
   * Prueft, ob eine gespeicherte PIN fuer diesen Passport vorliegt.
   * @param passport der Passport.
   * @return die PIN oder null, wenn keine gefunden wurde.
   * @throws Exception
   */
  private static String getCachedPIN(HBCIPassport passport) throws Exception
  {
    String key = getCacheKey(passport);

    // Kein Key - dann brauchen wir auch nicht im Cache schauen
    if (key == null)
      return null;

    byte[] data = (byte[]) pinCache.get(key);

    // Haben wir Daten?
    if (data == null)
      return null;
    
    ByteArrayInputStream bis  = new ByteArrayInputStream(data);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Application.getSSLFactory().decrypt(bis,bos);
    String s = bos.toString();
    if (s != null && s.length() > 0)
      return s;
    return null;
  }
  
  /**
   * Speichert die PIN temporaer fuer diese Session.
   * @param passport der Passport.
   * @param pin die PIN.
   * @throws Exception
   */
  private static void setCachedPIN(HBCIPassport passport, String pin) throws Exception
  {
    String key = getCacheKey(passport);
    
    // Kein Key, dann muessen wir nicht cachen
    if (key == null)
      return;
    
    byte[] data = pin.getBytes();
    
    ByteArrayInputStream bis  = new ByteArrayInputStream(data);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Application.getSSLFactory().encrypt(bis,bos);
    pinCache.put(key,bos.toByteArray());
  }
  
  /**
   * Loescht den PIN-Cache.
   * BUGZILLA 349
   */
  public static void clearPINCache()
  {
    pinCache.clear();
  }
  
  /**
   * Hilfsfunktion zum Ermitteln des Keys, zu dem die PIN gespeichert ist.
   * @param passport
   * @return die PIN oder null.
   * @throws Exception
   */
  private static String getCacheKey(HBCIPassport passport) throws Exception
  {
    // Entweder das Cachen ist abgeschaltet oder wir haben keinen Passport
    if (!Settings.getCachePin() || passport == null)
    {
      Logger.debug("pin caching disabled or no passport set");
      return null;
    }

    String key = null;
    // PIN/TAN
    if (passport instanceof HBCIPassportPinTan)
      key = ((HBCIPassportPinTan)passport).getFileName();
      
    // Schluesseldiskette
    else if (passport instanceof AbstractRDHSWFileBasedPassport)
      key = ((AbstractRDHSWFileBasedPassport)passport).getFilename();
      
    // DDV
    else if (passport instanceof HBCIPassportDDV)
      key = ((HBCIPassportDDV)passport).getFileName();

    if (key != null)
    {
      Logger.debug("using cache key: " + key);
      return key;
    }

    Logger.warn("unknown passport type [" + passport.getClass().getName() + "], don't know, how to cache pin");
    return null;
  }
  
  
}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.35  2009/08/10 10:22:09  willuhn
 * @N Als Cache-Key wird jetzt nur noch Pfad+Dateiname des Passports verwendet. Das ist erheblich einfacher zu handeln und erspart das Oeffnen des Passports
 *
 * Revision 1.34  2009/03/31 11:01:41  willuhn
 * @R Speichern des PIN-Hashes komplett entfernt
 *
 * Revision 1.33  2008/03/11 23:13:11  willuhn
 * @B Fix wegen falscher Pin (siehe Mail von Alexander vom 11.03.)
 *
 * Revision 1.32  2008/02/27 16:12:57  willuhn
 * @N Passwort-Dialog fuer Schluesseldiskette mit mehr Informationen (Konto, Dateiname)
 *
 * Revision 1.31  2007/12/21 17:37:39  willuhn
 * @N Update auf HBCI4Java 2.5.6
 *
 * Revision 1.30  2007/03/22 23:43:37  willuhn
 * @B Bug 322
 *
 * Revision 1.29  2007/02/21 12:10:36  willuhn
 * Bug 349
 *
 * Revision 1.28  2006/11/16 22:57:33  willuhn
 * @N gecachte PINs/Passwoerte werden nun nur noch einmal verwendet. Stimmen sie nicht, muss der User entscheiden
 *
 * Revision 1.27  2006/10/23 15:16:12  willuhn
 * @B Passwort-Handling ueberarbeitet
 *
 * Revision 1.26  2006/08/03 15:32:35  willuhn
 * @N Bug 62
 *
 * Revision 1.25  2006/04/03 12:30:18  willuhn
 * @N new InternetConnectionDialog
 *
 * Revision 1.24  2006/02/23 22:14:58  willuhn
 * @B bug 200 (Speichern der Auswahl)
 *
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