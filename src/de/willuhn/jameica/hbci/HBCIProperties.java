/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.42 $
 * $Date: 2011/01/20 17:13:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.util.Date;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * enthaelt HBCI-Parameter.
 */
public class HBCIProperties
{

	private static Settings settings = new Settings(HBCIProperties.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  static
  {
    settings.setStoreWhenRead(false);
  }

	/**
	 * Liste der in DTAUS erlaubten Zeichen.
	 */
	public final static String HBCI_DTAUS_VALIDCHARS = settings.getString("hbci.dtaus.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.&-+*%/$üöäÜÖÄß"); 

  /**
   * Liste der in einer IBAN erlaubten Zeichen.
   */
  public final static String HBCI_IBAN_VALIDCHARS = settings.getString("hbci.iban.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

  /**
   * Liste der in einer BIC erlaubten Zeichen.
   */
  public final static String HBCI_BIC_VALIDCHARS = settings.getString("hbci.bic.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

	/**
   * Liste der in Bankleitzahlen erlaubten Zeichen.
   */
  public final static String HBCI_BLZ_VALIDCHARS = settings.getString("hbci.blz.validchars","0123456789"); 

  /**
   * BUGZILLA 280
   * Liste der in Kontonummern erlaubten Zeichen.
   */
  public final static String HBCI_KTO_VALIDCHARS = settings.getString("hbci.kto.validchars","0123456789"); 

  /**
   * Maximale Text-Laenge einer Verwendungszweck-Zeile.
   */
  public final static int HBCI_TRANSFER_USAGE_MAXLENGTH = settings.getInt("hbci.transfer.usage.maxlength",27);

  /**
   * Maximale Text-Laenge einer Verwendungszweck-Zeile fuer Auslandsueberweisungen.
   */
  public final static int HBCI_FOREIGNTRANSFER_USAGE_MAXLENGTH = settings.getInt("hbci.foreigntransfer.usage.maxlength",140);

  /**
   * Maximale Anzahl von Verwendungszwecken.
   */
  public final static int HBCI_TRANSFER_USAGE_MAXNUM = settings.getInt("hbci.transfer.usage.maxnum",14);

  /**
   * Maximale Laenge einer Kontonummer.
   * Sollte eigentlich 10-stellig sein, da die CRC-Pruefungen ohnehin
   * nur bis dahin gelten. Aber fuer den Fall, dass auch mal noch
   * VISA-Konten unterstuetzt werden, lass ich es vorerst mal auf
   * 15 Stellen stehen und deklarieren es als "weiches" Limit.
   */
  public final static int HBCI_KTO_MAXLENGTH_SOFT = settings.getInt("hbci.kto.maxlength.soft",15);
  
  /**
   * Das harte Limit fuer Kontonummern, die CRC-Checks bestehen sollen
   */
  public final static int HBCI_KTO_MAXLENGTH_HARD = settings.getInt("hbci.kto.maxlength.hard",10);

  /**
   * Maximale Laenge einer IBAN.
   */
  public final static int HBCI_IBAN_MAXLENGTH = settings.getInt("hbci.iban.maxlength",34);

  /**
   * Maximale Laenge einer BIC.
   */
  public final static int HBCI_BIC_MAXLENGTH = settings.getInt("hbci.bic.maxlength",11);

  
  // BUGZILLA #49 http://www.willuhn.de/bugzilla/show_bug.cgi?id=49
  /**
   * Reservierter Tag fuer "Monatsletzten".
   */
  public final static int HBCI_LAST_OF_MONTH = settings.getInt("hbci.lastofmonth",99);

  /**
   * Laenge von Bankleitzahlen.
   */
  public final static int HBCI_BLZ_LENGTH = settings.getInt("hbci.blz.maxlength",8);

	/**
	 * Maximale Text-Laenge fuer Namen.
	 */
	public final static int HBCI_TRANSFER_NAME_MAXLENGTH = settings.getInt("hbci.transfer.name.maxlength",27);

  // BUGZILLA 29 http://www.willuhn.de/bugzilla/show_bug.cgi?id=29
  /**
   * Default-Waehrungs-Bezeichnung in Deutschland. 
   */
  public final static String CURRENCY_DEFAULT_DE = settings.getString("currency.default.de","EUR");

  // BUGZILLA 28  http://www.willuhn.de/bugzilla/show_bug.cgi?id=28
  // BUGZILLA 659 http://www.willuhn.de/bugzilla/show_bug.cgi?id=659
	/**
	 * Maximale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MAXLENGTH = settings.getInt("hbci.pin.maxlength",20);
	
  /**
	 * Minimale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MINLENGTH = settings.getInt("hbci.pin.minlength",5);
	
  /**
   * Default-Anzahl von anzuzeigenden Tagen in der Umsatz-Preview.
   */
  public final static int UMSATZ_DEFAULT_DAYS = settings.getInt("umsatz.default.days",30);
  
  /**
   * Bereinigt einen Text um die nicht erlaubten Zeichen.
   * @param text zu bereinigender Text.
   * @param validChars Liste der erlaubten Zeichen.
   * @return bereinigter Text.
   */
  public final static String clean(String text, String validChars)
  {
    if (text == null || text.length() == 0)
      return text;

    StringBuffer sb = new StringBuffer();
    char[] chars = text.toCharArray();
    for (char c:chars)
    {
      if (HBCI_DTAUS_VALIDCHARS.contains(Character.toString(c)))
        sb.append(c);
    }
    return sb.toString();
  }
  
  /**
   * Prueft die uebergebenen Strings auf Vorhandensein nicht erlaubter Zeichen.
   * @param chars zu testende Zeichen.
   * @param validChars Liste der gueltigen Zeichen.
   * @throws ApplicationException
   */
  public final static void checkChars(String chars, String validChars) throws ApplicationException
  {
    if (chars == null || chars.length() == 0)
      return;
    char[] c = chars.toCharArray();
    for (int i=0;i<c.length;++i)
    {
      if (validChars.indexOf(c[i]) == -1)
        throw new ApplicationException(i18n.tr("Das Zeichen \"{0}\" darf in \"{1}\" nicht verwendet werden",new String[]{""+c[i],chars})); 
    }
  }

  /**
   * BUGZILLA 232
   * Prueft den uebergebenen String auf korrekte Laenge.
   * Hierbei wird auch geprueft, ob die Laenge nach dem HBCI-Escaping noch korrekt ist.
   * @param chars zu testende Zeichen.
   * @param maxLength die maximale Laenge.
   * @throws ApplicationException
   */
  public final static void checkLength(String chars, int maxLength) throws ApplicationException
  {
    if (chars == null || chars.length() == 0)
      return;
    
    // Erstmal schauen, ob der Text ohne Codierung vielleicht schon zu lang ist.
    if (chars.length() > maxLength)
      throw new ApplicationException(i18n.tr("Der Text \"{0}\" ist zu lang. Bitte geben Sie maximal {1} Zeichen ein", new String[]{chars,""+maxLength}));

    // Achtung: Jetzt kommts! Festhalten! ;)
    // In der deutschen Sprache gibt es keinen Grossbuchstaben von "ß".
    // Wird nun ein Text von Java in Grossbuchstaben umgewandelt (mittels String#toUpperCase())
    // bleibt nicht etwa das "ß" erhalten. Nein, es wird gegen "SS" ersetzt.
    // Haben wir nun einen String, der exakt maxLength lang ist und enthält er
    // ein "ß" wuerden wir das hier tolerieren, bei der Ausfuehrung des
    // Geschaeftsvorfalls wuerde es jedoch zu einem Fehler kommen, da dort
    // der Text automatisch in Grossbuchstaben umgewandelt wird (geschieht
    // bei HBCI generell), damit das "ß" gegen "SS" ersetzt wird und der
    // Text am Ende genau um ein Zeichen zu lang wird. Verrueckt, oder? ;)
    // Da ggf. auch mehrere "ß" enthalten sind, ersetzen wir alle und schauen
    // dann, wie lang der Text geworden ist.
    if (chars.indexOf("ß") != -1)
    {
      String s = chars.replaceAll("ß","ss");
      if (s.length() > maxLength)
        throw new ApplicationException(i18n.tr("Der Text \"{0}\" wird nach der HBCI-Kodierung (ß wird hierbei gegen SS ersetzt) zu lang.",chars));
    }
  }

  /**
   * Prueft die Gueltigkeit der BLZ/Kontonummer-Kombi anhand von Pruefziffern.
   * @see HBCIUtils#checkAccountCRC(java.lang.String, java.lang.String)
   * @param blz
   * @param kontonummer
   * @return true, wenn die Kombi ok ist.
   */
  public final static boolean checkAccountCRC(String blz, String kontonummer)
  {
    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return true;

    // Haben wir eine gueltige BLZ?
    if (blz == null || 
        blz.length() == 0 || 
        blz.length() != HBCI_BLZ_LENGTH)
    {
      Logger.warn("blz [" + blz + "] not defined or out of range, skip crc check");
      return true;
    }
    
    // Haben wir eine gueltige Kontonummer?
    if (kontonummer == null || 
        kontonummer.length() == 0 ||
        kontonummer.length() > HBCI_KTO_MAXLENGTH_HARD)
    {
      Logger.warn("account number [" + kontonummer + "] not defined out of range, skip crc check");
      return true;
    }
    
    try
    {
      if (!HBCIUtils.canCheckAccountCRC(blz))
        return true; // koennen wir nicht pruefen. Dann akzeptieren wir das so.
      return HBCIUtils.checkAccountCRC(blz, kontonummer);
    }
    catch (Exception e)
    {
      try
      {
        Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
        HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
        HBCIUtils.initThread(plugin.getHBCIPropetries(),plugin.getHBCICallback());

        if (!HBCIUtils.canCheckAccountCRC(blz))
          return true;
        return HBCIUtils.checkAccountCRC(blz, kontonummer);
      }
      catch (Exception e2)
      {
        Logger.error("unable to verify account crc number",e2);
        return true;
      }
    }
  }

  /**
   * Prueft die Gueltigkeit einer IBAN anhand von Pruefziffern.
   * @see HBCIUtils#checkIBANCRC(java.lang.String)
   * @param iban die IBAN.
   * @return true, wenn die IBAN ok ist.
   */
  public final static boolean checkIBANCRC(String iban)
  {
    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return true;
    try
    {
      if (iban == null || // Nichts angegeben
          iban.length() == 0 || // Nichts angegeben
          iban.length() > HBCI_IBAN_MAXLENGTH || // zu lang
          iban.length() <= HBCI_KTO_MAXLENGTH_HARD) // zu kurz
      {
        return false;
      }
      return HBCIUtils.checkIBANCRC(iban);
    }
    catch (NumberFormatException nfe)
    {
      // TODO HBCI4Java koennte diese Exception vermutlich auch selbst fangen und false liefern
      Logger.warn("invalid iban: " + nfe.getMessage());
      return false;
    }
    catch (Exception e)
    {
      try
      {
        Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
        HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
        HBCIUtils.initThread(plugin.getHBCIPropetries(),plugin.getHBCICallback());
        return HBCIUtils.checkIBANCRC(iban);
      }
      catch (Exception e2)
      {
        Logger.error("unable to verify iban crc number",e2);
        return true;
      }
    }
  }
  
  /**
   * Resettet die Uhrzeit eines Datums.
   * @param date das Datum.
   * @return das neue Datum.
   * @deprecated Bitte kuenftig direkt {@link DateUtil#startOfDay(Date)} verwenden.
   */
  public static Date startOfDay(Date date)
  {
    return DateUtil.startOfDay(date);
  }

  /**
   * Setzt die Uhrzeit eines Datums auf 23:59:59.999.
   * @param date das Datum.
   * @return das neue Datum.
   * @deprecated Bitte kuenftig direkt {@link DateUtil#endOfDay(Date)} verwenden.
   */
  public static Date endOfDay(Date date)
  {
    return DateUtil.endOfDay(date);
  }

  // disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log: HBCIProperties.java,v $
 * Revision 1.42  2011/01/20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.41  2010/06/14 23:00:59  willuhn
 * @C Dialog-Groesse angepasst
 * @N Datei-Auswahldialog mit nativem Ueberschreib-Hinweis
 *
 * Revision 1.40  2010/03/31 11:19:40  willuhn
 * @N Automatisches Entfernen nicht-zulaessiger Zeichen
 *
 * Revision 1.39  2009/10/26 15:58:54  willuhn
 * @C Account CRC check nur, wenn der Alg. bekannt ist
 *
 * Revision 1.38  2009/03/18 22:09:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2009/02/18 00:35:54  willuhn
 * @N Auslaendische Bankverbindungen im Adressbuch
 *
 * Revision 1.36  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 * Revision 1.35  2009/02/12 23:55:57  willuhn
 * @N Erster Code fuer Unterstuetzung von Auslandsueberweisungen: In Tabelle "umsatz" die Spalte "empfaenger_konto" auf 40 Stellen erweitert und Eingabefeld bis max. 34 Stellen, damit IBANs gespeichert werden koennen
 *
 * Revision 1.34  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.33  2008/11/30 22:33:56  willuhn
 * @N BUGZILLA 659 - Maximale PIN-Laenge nun 20 Zeichen
 *
 * Revision 1.32  2008/11/24 00:12:08  willuhn
 * @R Spezial-Umsatzparser entfernt - wird kuenftig direkt in HBCI4Java gemacht
 *
 * Revision 1.31  2008/11/04 11:55:16  willuhn
 * @N Update auf HBCI4Java 2.5.9
 *
 * Revision 1.30  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.29  2008/05/20 22:47:06  willuhn
 * @B "ß" wird bei Umwandlung in Grossbuchstaben zu "SS" und muss bei der Laengenpruefung daher doppelt gezaehlt werden
 *
 * Revision 1.28  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 **********************************************************************/