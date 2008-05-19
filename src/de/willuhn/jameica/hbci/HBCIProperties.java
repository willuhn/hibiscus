/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.28 $
 * $Date: 2008/05/19 22:35:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.util.Calendar;
import java.util.Date;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * enthaelt HBCI-Parameter.
 */
public class HBCIProperties
{

	private static Settings settings = new Settings(HBCIProperties.class);
  private static I18N i18n = null;
  
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    settings.setStoreWhenRead(false);
  }

	/**
	 * Liste der in DTAUS erlaubten Zeichen.
	 */
	public final static String HBCI_DTAUS_VALIDCHARS =
		settings.getString("hbci.dtaus.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.&-+*%/$üöäÜÖÄß"); 

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
   * Maximale Laenge einer Kontonummer.
   * Sollte eigentlich 10-stellig sein, da die CRC-Pruefungen ohnehin
   * nur bis dahin gelten. Aber fuer den Fall, dass auch mal noch
   * VISA-Konten unterstuetzt werden, lass ich es vorerst mal auf
   * 15 Stellen stehen (das ist das Datenbank-Limit) und deklarieren
   * es als "weiches" Limit.
   */
  public final static int HBCI_KTO_MAXLENGTH_SOFT = settings.getInt("hbci.kto.maxlength.soft",15);
  
  /**
   * Das harte Limit fuer Kontonummern, die CRC-Checks bestehen sollen
   */
  public final static int HBCI_KTO_MAXLENGTH_HARD = settings.getInt("hbci.kto.maxlength.hard",10);

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

  // BUGZILLA 28 http://www.willuhn.de/bugzilla/show_bug.cgi?id=28
	/**
	 * Maximale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MAXLENGTH = settings.getInt("hbci.pin.maxlength",10);
	
  /**
   * Ein ggf vorhandener Spezialparser fuer Umsaetze 
   */
  public final static String HBCI_TRANSFER_SPECIAL_PARSER = settings.getString("hbci.transfer.specialparser",null);

  /**
	 * Minimale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MINLENGTH = settings.getInt("hbci.pin.minlength",5);
	
  /**
   * Default-Anzahl von anzuzeigenden Tagen in der Umsatz-Preview.
   */
  public final static int UMSATZ_DEFAULT_DAYS = settings.getInt("umsatz.default.days",30);
  
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
    try
    {
      if (kontonummer == null || 
          kontonummer.length() == 0 ||
          kontonummer.length() > HBCI_KTO_MAXLENGTH_HARD)
      {
        Logger.warn("account number [" + kontonummer + "] out of range, skip crc check");
        return true;
      }
      return HBCIUtils.checkAccountCRC(blz, kontonummer);
    }
    catch (Exception e)
    {
      try
      {
        Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
        HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
        HBCIUtils.initThread(plugin.getResources().getClassLoader(),null,plugin.getHBCICallback());
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
   * Resettet die Uhrzeit eines Datums.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date startOfDay(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,0);
    cal.set(Calendar.MINUTE,0);
    cal.set(Calendar.SECOND,0);
    cal.set(Calendar.MILLISECOND,0);
    return cal.getTime();
  }

  /**
   * Setzt die Uhrzeit eines Datums auf 23:59:59.999.
   * @param date das Datum.
   * @return das neue Datum.
   */
  public static Date endOfDay(Date date)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date == null ? new Date() : date);
    cal.set(Calendar.HOUR_OF_DAY,23);
    cal.set(Calendar.MINUTE,59);
    cal.set(Calendar.SECOND,59);
    cal.set(Calendar.MILLISECOND,999);
    return cal.getTime();
  }

  // disabled
	private HBCIProperties()
	{
	}

}


/**********************************************************************
 * $Log: HBCIProperties.java,v $
 * Revision 1.28  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.27  2007/11/27 17:15:57  willuhn
 * @C HBCI4Java mit Classloader des Plugins initialisieren
 *
 * Revision 1.26  2007/09/11 15:10:35  willuhn
 * @N Default-Wert fuer maximale PIN-Laenge auf 10 erhoeht
 *
 * Revision 1.25  2007/07/26 18:26:05  willuhn
 * @B HBCIUtils.checkAccountCRCByAlg wirft eine ArrayIndexOutOfBoundsException bei Kontonummern mit mehr als 10 Stellen. Wir schreiben das nur in's Log, tolerieren es aber (nocht)
 *
 * Revision 1.24  2007/06/21 14:06:30  willuhn
 * @B reinit von HBCI4Java mit aktuellem Callback
 *
 * Revision 1.23  2007/06/01 15:20:52  willuhn
 * @B reinit hbci kernel on other threads
 *
 * Revision 1.22  2007/05/16 13:59:53  willuhn
 * @N Bug 227 HBCI-Synchronisierung auch im Fehlerfall fortsetzen
 * @C Synchronizer ueberarbeitet
 * @B HBCIFactory hat globalen Status auch bei Abbruch auf Error gesetzt
 *
 * Revision 1.21  2007/03/05 14:57:16  willuhn
 * @B zusaetzlichen Laengen-Check (Workaround zu Bug 232) entfernt (ist inzwischen in HBCI4Java gefixt)
 *
 * Revision 1.20  2007/02/26 12:48:23  willuhn
 * @N Spezial-PSD-Parser von Michael Lambers
 *
 * Revision 1.19  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.18  2006/12/27 17:56:49  willuhn
 * @B Bug 341
 *
 * Revision 1.17  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.16  2006/05/11 10:57:35  willuhn
 * @C merged Bug 232 into HEAD
 *
 * Revision 1.15.2.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.15  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 * Revision 1.14  2006/02/06 16:03:50  willuhn
 * @B bug 163
 *
 * Revision 1.13  2005/09/25 23:15:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2005/08/22 10:36:37  willuhn
 * @N bug 115, 116
 *
 * Revision 1.10  2005/06/07 22:19:57  web0
 * @B bug 49
 *
 * Revision 1.9  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.8  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.7  2005/03/30 23:26:28  web0
 * @B bug 29
 * @B bug 30
 *
 * Revision 1.6  2005/03/25 23:08:44  web0
 * @B bug 28
 *
 * Revision 1.5  2005/03/09 01:16:17  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2004/11/02 18:48:32  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/01 23:10:19  willuhn
 * @N Pruefung auf gueltige Zeichen in Verwendungszweck
 *
 **********************************************************************/