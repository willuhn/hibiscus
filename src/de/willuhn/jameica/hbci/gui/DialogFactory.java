/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/DialogFactory.java,v $
 * $Revision: 1.38 $
 * $Date: 2011/05/24 09:06:11 $
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

import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.AccountContainerDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PINDialog;
import de.willuhn.jameica.hbci.passports.rdh.KeyPasswordLoadDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;

/**
 * Hilfsklasse zur Erzeugung von Hilfs-Dialogen bei der HBCI-Kommunikation.
 */
public class DialogFactory
{

  // BUGZILLA 185
  private static Hashtable pinCache = new Hashtable();

  private static long lastTry = 0;

  /**
	 * Erzeugt den PIN-Dialog.
   * @param passport Passport, fuer den die PIN-Abfrage gemacht wird.
	 * @return die eingegebene PIN.
   * @throws Exception
	 */
	public static synchronized String getPIN(HBCIPassport passport) throws Exception
	{
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

    PINDialog dialog = new PINDialog(passport);
    pin = (String) dialog.open();
    setCachedPIN(passport,pin);
    return pin;
	}

	/**
	 * Dialog zur Eingabe des Passworts fuer Schluesseldateien.
   * @param passport der HBCI-Passport.
   * @return eingegebenes Passwort.
   * @throws Exception
   */
  public static synchronized String getKeyPassword(HBCIPassport passport) throws Exception
	{
    HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
    boolean forceAsk = plugin.getResources().getSettings().getBoolean("hbcicallback.askpassphrase.force",false);

    boolean secondTry = System.currentTimeMillis() - lastTry < 200l;
    if (secondTry) Logger.warn("cached key seems to be wrong, asking user, passport: " + passport.getClass().getName());
    
    if (!forceAsk && !secondTry)
    {
      String pw = getCachedPIN(passport);
      if (pw != null)
      {
        Logger.info("using cached password, passport: " + passport.getClass().getName());
        lastTry = System.currentTimeMillis();
        return pw; // wir haben ein gecachtes Passwort, das nehmen wir
      }
    }
    lastTry = 0;

    // Wir haben nichts im Cache oder wurden explizit aufgefordert, nach dem Passwort zu fragen
    KeyPasswordLoadDialog dialog = new KeyPasswordLoadDialog(passport);
    String pw = (String) dialog.open();
    setCachedPIN(passport,pw);
    return pw;
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
		AccountContainerDialog dialog = new AccountContainerDialog(p);
    return (AccountContainer) dialog.open();
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

    byte[] data = null;
    
    // Cache checken
    if (Settings.getCachePin())
    {
      data = (byte[]) pinCache.get(key);
    }

    // Wenn wir noch nichts im Cache haben, schauen wir im Wallet - wenn das erlaubt ist
    if (data == null && Settings.getStorePin())
    {
      String s = (String) Settings.getWallet().get(key);
      if (s != null)
      {
        data = Base64.decode(s);
        
        // Wenn diese Meldung im Log erscheint, gibts keinen Support mehr von mir.
        // Wer die PIN permament speichert, tut das auf eigenes Risiko
        Logger.info("pin loaded from wallet");
        // Uebernehmen wir gleich in den Cache, damit wir beim
        // naechsten Mal direkt im Cache schauen koennen und nicht
        // mehr im Wallet
        pinCache.put(key,data);
      }
    }

    // Haben wir Daten?
    if (data != null)
    {
      ByteArrayInputStream bis  = new ByteArrayInputStream(data);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().decrypt(bis,bos);
      String s = bos.toString();
      if (s != null && s.length() > 0)
        return s;
    }
    
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
    
    ByteArrayInputStream bis  = new ByteArrayInputStream(pin.getBytes());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Application.getSSLFactory().encrypt(bis,bos);
    byte[] crypted = bos.toByteArray();
    
    if (Settings.getCachePin())
    {
      pinCache.put(key,crypted);
    }
    
    // Permanentes Speichern der PIN gibts nur bei PIN/TAN, da dort ueber
    // die TAN eine weitere Autorisierung bei der Ausfuehrung von Geschaeftsvorfaellen
    // mit Geldfluss stattfindet. Bei DDV/RDH koennte man sonst beliebig Geld
    // transferieren, ohne jemals wieder nach einem Passwort gefragt zu werden.
    if (Settings.getStorePin() && (passport instanceof HBCIPassportPinTan))
    {
      // Nicht direkt das Byte-Array speichern sondern einen Base64-String.
      // Grund: Bei einem Byte-Array wuerde der XML-Serializer fuer jedes
      // Byte ein einzelnes XML-Element anlegen und damit das Wallet aufblasen
      Settings.getWallet().set(key,Base64.encode(crypted));
    }
  }
  
  /**
   * Loescht den PIN-Cache.
   * BUGZILLA 349
   */
  public static void clearPINCache()
  {
    pinCache.clear();

    // Wir loeschen auch die gespeicherten PINs
    // Unabhaengig davon, ob das Feature gerade aktiviert ist oder nicht.
    try
    {
      Settings.getWallet().deleteAll("hibiscus.pin.");
    }
    catch (Exception e)
    {
      // Wenn das fehlschlaegt, sollte man eigentlich mehr Alarm schlagen
      // Allerdings wuesste ich jetzt auch nicht, was der User dann machen
      // kann, ausser dem Loeschen der Wallet-Datei. Was aber dazu fuehren
      // wuerde, dass saemtliche DDV- und PinTan-Passport-Files nicht mehr
      // gelesen werden koennen, weil fuer die ja Random-Passworte verwendet
      // werden, die ebenfalls im Wallet gespeichert sind
      Logger.error("unable to clear pin cache",e);
    }
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
      key = "hibiscus.pin." + key;
      Logger.debug("using cache key: " + key);
      return key;
    }

    Logger.warn("unknown passport type [" + passport.getClass().getName() + "], don't know, how to cache pin");
    return null;
  }
  
  
}


/**********************************************************************
 * $Log: DialogFactory.java,v $
 * Revision 1.38  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.37  2011-05-23 12:57:38  willuhn
 * @N optionales Speichern der PINs im Wallet. Ich announce das aber nicht. Ich hab das nur eingebaut, weil mir das Gejammer der User auf den Nerv ging und ich nicht will, dass sich User hier selbst irgendwelche Makros basteln, um die PIN dennoch zu speichern
 *
 * Revision 1.36  2010-07-24 00:22:48  willuhn
 * *** empty log message ***
 **********************************************************************/