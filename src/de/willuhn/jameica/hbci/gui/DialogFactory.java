/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.kapott.hbci.passport.AbstractRDHSWFileBasedPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportChipcard;
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
  private static Map<String,PINEntry> pinCache = new HashMap<String,PINEntry>();

  private static long lastTry = 0;

  /**
	 * Liefert die PIN.
   * @param passport Passport, fuer den die PIN-Abfrage gemacht wird.
	 * @return die eingegebene PIN.
   * @throws Exception
	 */
	public static synchronized String getPIN(HBCIPassport passport) throws Exception
	{
    boolean secondTry = System.currentTimeMillis() - lastTry < 600l;
    if (secondTry)
    {
      Logger.warn("cached pin seems to be wrong, asking user, passport: " + passport.getClass().getName());
      dirtyPINCache(passport);
    }

    PINEntry entry = getCachedPIN(passport);
    String pin = entry != null ? entry.getPIN() : null;
    if (pin != null && !secondTry && !entry.dirty)
    {
      Logger.info("using cached pin, passport: " + passport.getClass().getName());
      lastTry = System.currentTimeMillis();
      return pin;
    }
    lastTry = 0;

    PINDialog dialog = new PINDialog(pin);
    pin = (String) dialog.open();
    setCachedPIN(passport,pin); // Das entfernt auch gleich die Dirty-Markierung
    return pin;
	}

	/**
	 * Liefert das Passwort fuer Schluesseldateien.
   * @param passport der HBCI-Passport.
   * @return eingegebenes Passwort.
   * @throws Exception
   */
  public static synchronized String getKeyPassword(HBCIPassport passport) throws Exception
	{
    HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
    boolean forceAsk = plugin.getResources().getSettings().getBoolean("hbcicallback.askpassphrase.force",false);

    boolean secondTry = System.currentTimeMillis() - lastTry < 600l;
    if (secondTry)
    {
      Logger.warn("cached key seems to be wrong, asking user, passport: " + passport.getClass().getName());
      dirtyPINCache(passport);
    }
    
    String pw = null;
    
    if (!forceAsk && !secondTry)
    {
      PINEntry entry = getCachedPIN(passport);
      pw = entry != null ? entry.getPIN() : null;
      if (pw != null && !entry.dirty)
      {
        Logger.info("using cached password, passport: " + passport.getClass().getName());
        lastTry = System.currentTimeMillis();
        return pw; // wir haben ein gecachtes Passwort, das nehmen wir
      }
    }
    lastTry = 0;

    // Wir haben nichts im Cache oder wurden explizit aufgefordert, nach dem Passwort zu fragen
    KeyPasswordLoadDialog dialog = new KeyPasswordLoadDialog(passport,pw);
    pw = (String) dialog.open();
    if (!forceAsk) // Nur cachen, wenn die Passwort-Abfrage nicht erzwungen war
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
   * @return der PIN-Entry oder null, wenn keine gefunden wurde.
   * @throws Exception
   */
  private static PINEntry getCachedPIN(HBCIPassport passport) throws Exception
  {
    String key = getCacheKey(passport);

    // Kein Key - dann brauchen wir auch nicht im Cache schauen
    if (key == null)
      return null;

    PINEntry entry = null;
    
    // Cache checken - ob der fuer die ganze Sitzung stehen bleibt oder nur fuer die
    // Dauer der Synchronisierung, das entscheiden wir nicht hier sondern am Ende der
    // Synchronisierung
    entry = pinCache.get(key);
    
    // Wenn wir noch nichts im Cache haben, schauen wir im Wallet - wenn das erlaubt ist
    if (entry == null && Settings.getStorePin() && (passport instanceof HBCIPassportPinTan))
    {
      String s = (String) Settings.getWallet().get(key);
      if (s != null)
      {
        byte[] data = Base64.decode(s);
        
        Logger.info("pin loaded from wallet");

        // Uebernehmen wir gleich in den Cache, damit wir beim
        // naechsten Mal direkt im Cache schauen koennen und nicht
        // mehr im Wallet
        entry = new PINEntry(data);
        pinCache.put(key,entry);
      }
    }
    
    return entry;
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

    // in Cache legen - ob der fuer die ganze Sitzung stehen bleibt oder nur fuer die
    // Dauer der Synchronisierung, das entscheiden wir nicht hier sondern am Ende der
    // Synchronisierung
    PINEntry entry = new PINEntry(pin);
    pinCache.put(key,entry);
    
    // Permanentes Speichern der PIN gibts nur bei PIN/TAN, da dort ueber
    // die TAN eine weitere Autorisierung bei der Ausfuehrung von Geschaeftsvorfaellen
    // mit Geldfluss stattfindet. Bei DDV/RDH koennte man sonst beliebig Geld
    // transferieren, ohne jemals wieder nach einem Passwort gefragt zu werden.
    if (entry != null && Settings.getStorePin() && (passport instanceof HBCIPassportPinTan))
    {
      // Nicht direkt das Byte-Array speichern sondern einen Base64-String.
      // Grund: Bei einem Byte-Array wuerde der XML-Serializer fuer jedes
      // Byte ein einzelnes XML-Element anlegen und damit das Wallet aufblasen
      Settings.getWallet().set(key,Base64.encode(entry.crypted));
    }
  }
  
  /**
   * Loescht den PIN-Cache.
   * BUGZILLA 349
   * @param passport der Passport, dessen PIN geloescht werden soll.
   * Optional. Wird er weggelassen, werden alle PINs geloescht.
   */
  public static void clearPINCache(HBCIPassport passport)
  {
    if (passport != null)
    {
      // Wir loeschen nur das Passwort vom angegebenen Passport
      String key = getCacheKey(passport);
      if (key != null)
      {
        Logger.info("pin cache cleared for single passport");
        pinCache.remove(key);
      }
      // Wenn kein Key existiert, haben wir auch nichts zu loeschen,
      // weil dann gar kein Passwort im Cache existieren kann
    }
    else
    {
      // Kompletten Cache loeschen
      Logger.info("pin cache cleared for all passports");
      pinCache.clear();
    }

    // Wir loeschen auch die gespeicherten PINs
    // Unabhaengig davon, ob das Feature gerade aktiviert ist oder nicht.
    // Denn ohne Cache gibts auch keinen Store.
    clearPINStore(passport);
  }
  
  /**
   * Markiert die PIN des Passports als Dirty - zum Beispiel aufgrund eines Fehlers.
   * Das fuehrt dazu, dass die PIN beim naechsten Mal neu erfragt wird, aber im Passwort-Dialog
   * bereits vorbefuellt ist.
   * @param passport der Passport, dessen PIN invalidiert werden soll.
   * Optional. Wird er weggelassen, werden alle PINs invalidiert.
   */
  public static void dirtyPINCache(HBCIPassport passport)
  {
    if (passport != null)
    {
      String key = getCacheKey(passport);
      if (key == null)
        return;

      PINEntry entry = pinCache.get(key);
      if (entry == null)
        return;
      
      Logger.warn("mark pin cache dirty for single passport");
      entry.dirty = true;
      return;
    }
    else
    {
      // Alle PINs invalidieren
      Logger.warn("mark pin cache dirty for all passports");
      for (PINEntry e:pinCache.values())
      {
        e.dirty = true;
      }
    }
  }
  
  /**
   * Loescht den permanenten Store mit den PINs.
   * @param passport der Passport, dessen PIN geloescht werden soll.
   * Optional. Wird er weggelassen, werden alle PINs geloescht.
   */
  public static void clearPINStore(HBCIPassport passport)
  {
    try
    {
      if (passport != null)
      {
        // Wir loeschen nur das Passwort vom angegebenen Passport
        String key = getCacheKey(passport);
        if (key != null && Settings.getWallet().get(key) != null)
        {
          // Nur loeschen, wenn es den Key auch wirklich gibt. Das spart
          // den Schreibzugriff, wenn er nicht vorhanden ist
          Logger.info("pin store cleared for single passport");
          Settings.getWallet().delete(key);
        }
        // Wenn kein Key existiert, haben wir auch nichts zu loeschen,
        // weil dann gar kein Passwort im Store existieren kann
      }
      else
      {
        // Alles Keys beginnen mit "hibiscus.pin."
        Logger.info("pin store cleared for all passports");
        Settings.getWallet().deleteAll("hibiscus.pin.");
      }
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
   */
  private static String getCacheKey(HBCIPassport passport)
  {
    // Entweder das Cachen ist abgeschaltet oder wir haben keinen Passport
    if (passport == null)
    {
      Logger.debug("no passport given - unable to generate cache key");
      return null;
    }

    String key = null;
    // PIN/TAN
    if (passport instanceof HBCIPassportPinTan)
      key = ((HBCIPassportPinTan)passport).getFileName();
      
    // Schluesseldatei
    else if (passport instanceof AbstractRDHSWFileBasedPassport)
      key = ((AbstractRDHSWFileBasedPassport)passport).getFilename();
      
    // Chipkarte
    else if (passport instanceof HBCIPassportChipcard)
    {
      key = ((HBCIPassportChipcard)passport).getFileName();
      if (key == null)
      {
        Logger.info("have no passport filename for type [" + passport.getClass().getName() + "], pin cannot be cached");
        return null;
      }
    }

    if (key != null)
    {
      key = "hibiscus.pin." + key;
      Logger.debug("using cache key: " + key);
      return key;
    }

    Logger.warn("unknown passport type [" + passport.getClass().getName() + "], don't know, how to cache pin");
    return null;
  }
  
  /**
   * Kapselt einen PIN-Eintrag.
   */
  private static class PINEntry
  {
    private byte[] crypted;
    private boolean dirty = false;
    
    /**
     * ct.
     * @param pin die PIN.
     * @throws Exception
     */
    private PINEntry(String pin) throws Exception
    {
      ByteArrayInputStream bis  = new ByteArrayInputStream(pin.getBytes());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().encrypt(bis,bos);
      this.crypted = bos.toByteArray();
    }

    /**
     * ct.
     * @param crypted die verschluesselte PIN.
     * @throws Exception
     */
    private PINEntry(byte[] crypted) throws Exception
    {
      this.crypted = crypted;
    }

    /**
     * Liefert die PIN.
     * @return die PIN.
     * @throws Exception
     */
    private String getPIN() throws Exception
    {
      if (this.crypted == null)
        return null;
      
      ByteArrayInputStream bis  = new ByteArrayInputStream(this.crypted);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Application.getSSLFactory().decrypt(bis,bos);
      String s = bos.toString();
      return s != null && s.length() > 0 ? s : null;
    }
  }
  
}
