/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.IBANCode;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.jost_net.OBanToo.SEPA.SEPAException.Fehler;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
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
   * Liste der in SWIFT erlaubten Zeichen.
   * Siehe http://www.hbci-zka.de/dokumente/spezifikation_deutsch/FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf
   * Absatz B
   */
  // public final static String HBCI_SWIFT_VALIDCHARS = settings.getString("hbci.swift.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 '()+,-./:?{}"); 

	/**
   * Liste der in SEPA erlaubten Zeichen.
   * Siehe http://www.ebics.de/fileadmin/unsecured/anlage3/anlage3_spec/Anlage_3_DatenformateV2.6.pdf
   * Absatz 2.1, BUGZILLA 1244
   */
  public final static String HBCI_SEPA_VALIDCHARS = settings.getString("hbci.sepa.validchars", "abcdefghijklmnopqrstuvwxyzüöäßABCDEFGHIJKLMNOPQRSTUVWXYZÜÖÄ0123456789':?,- (+.)/&*$%");

  /**
   * Liste der fuer die Mandate-ID gueltigen Zeichen. RestrictedIdentificationSEPA2.
   */
  public final static String HBCI_SEPA_MANDATE_VALIDCHARS = settings.getString("hbci.sepa.mandate.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789':?,-(+.)/");

  /**
   * Liste der fuer die Referenz gueltigen Zeichen. RestrictedIdentificationSEPA1.
   */
  public final static String HBCI_SEPA_PMTINF_VALIDCHARS = HBCI_SEPA_MANDATE_VALIDCHARS + " ";

  /**
   * Liste der in einer IBAN erlaubten Zeichen.
   */
  public final static String HBCI_IBAN_VALIDCHARS = settings.getString("hbci.iban.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

  /**
   * Liste der in einer BIC erlaubten Zeichen.
   */
  public final static String HBCI_BIC_VALIDCHARS = settings.getString("hbci.bic.validchars", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

  /**
   * Liste der in Purpose-Codes erlaubten Zeichen.
   */
  public final static String HBCI_SEPA_PURPOSECODE_VALIDCHARS = settings.getString("hbci.sepa.purposecode.validchars", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"); 

  /**
   * Liste der in Bankleitzahlen erlaubten Zeichen.
   */
  public final static String HBCI_BLZ_VALIDCHARS = settings.getString("hbci.blz.validchars","0123456789"); 

  /**
   * Liste der in der BZÜ-Pruefziffer erlaubten Zeichen.
   */
  public final static String HBCI_BZU_VALIDCHARS = settings.getString("hbci.bzu.validchars","0123456789"); 

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
   * Maximale Text-Laenge einer Verwendungszweck-Zeile.
   */
  public final static int HBCI_TRANSFER_USAGE_DB_MAXLENGTH = settings.getInt("hbci.transfer.usage.db.maxlength",35);

  /**
   * Maximale Laenge eines GV-Code.
   */
  public final static int HBCI_GVCODE_MAXLENGTH = settings.getInt("hbci.gvcode.maxlength",3);

  /**
   * Maximale Laenge eines Textschluessels.
   */
  public final static int HBCI_ADDKEY_MAXLENGTH = settings.getInt("hbci.addkey.maxlength",3);

  /**
   * Laenge der Pruefziffern bei BZÜ-Ueberweisung.
   */
  public final static int HBCI_TRANSFER_BZU_LENGTH = settings.getInt("hbci.transfer.bzu.length",13);

  /**
   * Maximale Text-Laenge einer Verwendungszweck-Zeile bei SEPA-Auftraegen.
   */
  public final static int HBCI_SEPATRANSFER_USAGE_MAXLENGTH = settings.getInt("hbci.foreigntransfer.usage.maxlength",140);

  /**
   * Maximale Anzahl von Verwendungszwecken.
   */
  public final static int HBCI_TRANSFER_USAGE_MAXNUM = settings.getInt("hbci.transfer.usage.maxnum",14);

  /**
   * Maximale Laenge einer Kontonummer.
   * Sollte eigentlich 10-stellig sein, da die CRC-Pruefungen ohnehin
   * nur bis dahin gelten. Aber fuer den Fall, dass auch mal noch
   * VISA-Konten unterstuetzt werden, lass ich es vorerst mal auf
   * 16 Stellen stehen und deklarieren es als "weiches" Limit.
   */
  public final static int HBCI_KTO_MAXLENGTH_SOFT = settings.getInt("hbci.kto.maxlength.soft",16);
  
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

  /**
   * Maximale Laenge des Datentyps "ID" in der HBCI-Spec.
   */
  public final static int HBCI_ID_MAXLENGTH = settings.getInt("hbci.id.maxlength",30);

  
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
	public final static int HBCI_TRANSFER_NAME_MAXLENGTH = settings.getInt("hbci.transfer.name.maxlength",70);

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
	public final static int HBCI_PIN_MAXLENGTH = settings.getInt("hbci.pin.maxlength",50);
	
  /**
	 * Minimale Laenge fuer PINs.
	 */
	public final static int HBCI_PIN_MINLENGTH = settings.getInt("hbci.pin.minlength",5);
	
  /**
   * Default-Anzahl von anzuzeigenden Tagen in der Umsatz-Preview.
   */
  public final static int UMSATZ_DEFAULT_DAYS = settings.getInt("umsatz.default.days",30);
  
  /**
   * Maximale Laenge der EndtoEnd-ID bei SEPA.
   */
  public final static int HBCI_SEPA_ENDTOENDID_MAXLENGTH = settings.getInt("hbci.sepa.endtoendid.maxlength",35);
  
  /**
   * Maximale Laenge des Purpose-Codes bei SEPA.
   */
  public final static int HBCI_SEPA_PURPOSECODE_MAXLENGTH = settings.getInt("hbci.sepa.purposecode.maxlength",4);

  /**
   * Maximale Laenge der Mandate-ID bei SEPA.
   */
  public final static int HBCI_SEPA_MANDATEID_MAXLENGTH = settings.getInt("hbci.sepa.mandateid.maxlength",35);

  /**
   * Maximale Laenge der Glaeubiger-ID bei SEPA.
   */
  public final static int HBCI_SEPA_CREDITORID_MAXLENGTH = settings.getInt("hbci.sepa.creditorid.maxlength",35);

  /**
   * SEPA-Tags parsen?
   */
  public final static boolean HBCI_SEPA_PARSE_TAGS = settings.getBoolean("hbci.sepa.parsetags",true);

  /**
   * Text-Replacements fuer SEPA.
   * Die in SEPA nicht zulaessigen Zeichen "{@code &*%$üöäÜÖÄß}" werden ersetzt.
   */
  public final static String[][] TEXT_REPLACEMENTS_SEPA = new String[][] {new String[]{"&","*","%","$","ü", "ö", "ä", "Ü", "Ö", "Ä", "ß"},
                                                                          new String[]{"+",".",".",".","ue","oe","ae","Ue","Oe","Ae","ss"}};

  /**
   * Text-Replacements fuer Umsatz-Properties.
   */
  public final static String[][] TEXT_REPLACEMENTS_UMSATZ = new String[][] {new String[]{"\n","\r"},
                                                                            new String[]{""  ,""}};
  
  private final static Map<Fehler,String> obantooCodes = new HashMap<Fehler,String>()
  {{
    put(Fehler.BLZ_LEER,                                    i18n.tr("Keine BLZ angegeben"));
    put(Fehler.BLZ_UNGUELTIGE_LAENGE,                       i18n.tr("BLZ nicht achtstellig"));
    put(Fehler.BLZ_UNGUELTIG,                               i18n.tr("BLZ unbekannt"));
    put(Fehler.KONTO_LEER,                                  i18n.tr("Keine Kontonummer angegeben"));
    put(Fehler.KONTO_UNGUELTIGE_LAENGE,                     i18n.tr("Länge der Kontonummer ungültig"));
    put(Fehler.KONTO_PRUEFZIFFER_FALSCH,                    i18n.tr("Prüfziffer der Kontonummer falsch"));
    put(Fehler.KONTO_PRUEFZIFFERNREGEL_NICHT_IMPLEMENTIERT, i18n.tr("Prüfziffern-Verfahren der Kontonummer unbekannt"));
    put(Fehler.IBANREGEL_NICHT_IMPLEMENTIERT,               i18n.tr("IBAN-Regel unbekannt"));
    put(Fehler.UNGUELTIGES_LAND,                            i18n.tr("Land unbekannt"));
  }};
  
  private final static List<Fehler> ignoredErrors = new ArrayList<Fehler>()
  {{
    // Siehe BUGZILLA 1569
    add(Fehler.UNGUELTIGES_LAND);
  }};


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
      if (validChars.contains(Character.toString(c)))
        sb.append(c);
    }
    return sb.toString();
  }
  
  /**
   * Ersetzt im Text Strings entsprechend der Replacements. 
   * @param text der Text mit den zu ersetzenden Zeichen.
   * @param replacements die Ersetzungen.
   * @return der Text mit den ersetzten Zeichen.
   * @see HBCIProperties#TEXT_REPLACEMENTS_SEPA
   */
  public final static String replace(String text, String[][] replacements)
  {
    if (text == null || text.length() == 0)
      return text;
    
    return StringUtils.replaceEach(text,replacements[0],replacements[1]);
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
        throw new ApplicationException(i18n.tr("Das Zeichen \"{0}\" darf in \"{1}\" nicht verwendet werden", "" + c[i], chars));
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
      throw new ApplicationException(i18n.tr("Der Text \"{0}\" ist zu lang. Bitte geben Sie maximal {1} Zeichen ein", chars, ""+maxLength));
  }
  
  /**
   * Gruppiert den String alle <code>len</code> Zeichen in Bloecke, die durch den
   * String <code>sep</code> getrennt sind.  
   * @param s der zu gruppierende String.
   * @param len Anzahl der Zeichen pro Gruppe.
   * @param sep das Trennzeichen. Falls null, wird ein Leerzeichen als Trenner verwendet.
   * @return der gruppierte String.
   */
  public final static String group(String s, int len, String sep)
  {
    if (s == null)
      return "";
    
    if (sep == null)
      sep = " ";
    return s.replaceAll("(.{" + len + "})", "$0" + sep).trim();
  }
  
  /**
   * Gruppiert eine IBAN in Gruppen zu je 4 Zeichen und schreibt die ersten
   * beiden Buchstaben (Laenderkennzeichen) gross.
   * @param s die IBAN.
   * @return die formatierte Darstellung.
   */
  public final static String formatIban(String s)
  {
    if (s == null)
      return "";

    // Wenn der Text irgendwas ausser Zahlen, Buchstaben und Leerzeichen enthaelt,
    // formatieren wir es nicht. Dann ist es keine IBAN
    try
    {
      checkChars(s,HBCI_IBAN_VALIDCHARS + " ");
      s = s.replaceAll(" ", "");
      return group(s,4," ").toUpperCase();
    }
    catch (ApplicationException ae)
    {
      return s;
    }
  }
  
  /**
   * Ermittelt zu einer BIC oder BLZ den Namen der Bank.
   * @param bic die BIC oder BLZ.
   * @return der Name der Bank oder ein Leerstring, wenn nicht ermittelbar.
   * Niemals NULL sondern hoechstens ein Leerstring.
   */
  public final static String getNameForBank(String bic)
  {
    bic = StringUtils.trimToNull(bic);
    if (bic == null)
      return null;
    
    Bank bank = null;
    
    // Wenn sie 8 Zeichen lang ist, gehen wir davon aus, dass es eine BLZ ist.
    // Sonst versuchen wir es als BIC zu interpretieren.
    if (bic.length() == HBCI_BLZ_LENGTH)
      bank = Banken.getBankByBLZ(bic);
    else
      bank = Banken.getBankByBIC(bic);
    
    if (bank == null)
      return null;

    // Text einkuerzen, wenn er zu lang ist.
    // Normalerweise nicht noetig. Es gibt aber einige Banken, die z.Bsp. folgenden
    // Namen haben: "Landesbank Baden-Württemberg/Baden-Württembergische Bank"
    // Das verzerrt sonst die Layouts an einigen Stellen
    return StringUtils.abbreviateMiddle(bank.getBezeichnung(),"...",24);
  }
  
  /**
   * Liefert die Bankdaten fuer die Bank.
   * @param blz die BLZ.
   * @return die Bankdaten. NULL, wenn sie nicht gefunden wurden.
   */
  public static BankInfo getBankInfo(String blz)
  {
    return HBCIUtils.getBankInfo(blz);
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
    
    
    // einen Fehlversuch erlauben wir. Das kann passieren, wenn HBCI4Java
    // noch nicht fuer den aktuellen Thread initialisiert ist.
    for (int i=0;i<2;++i)
    {
      try
      {
        if (!HBCIUtils.canCheckAccountCRC(blz))
          return true; // koennen wir nicht pruefen. Dann akzeptieren wir das so.
        if (HBCIUtils.checkAccountCRC(blz, kontonummer))
          return true; // CRC-Pruefung bestanden
        
        if (!de.willuhn.jameica.hbci.Settings.getKontoCheckExcludeAddressbook())
          return false; // CRC-Pruefung nicht bestanden und wir sollen nicht im Adressbuch nachsehen

        // OK, wir schauen im Adressbuch
        DBService db = de.willuhn.jameica.hbci.Settings.getDBService();
        HibiscusAddress address = (HibiscusAddress) db.createObject(HibiscusAddress.class,null);
        address.setBlz(blz);
        address.setKontonummer(kontonummer);
        AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
        return (service.contains(address) != null);
      }
      catch (Exception e)
      {
        if (i == 0)
        {
          try
          {
            Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
            HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
            HBCIUtils.initThread(plugin.getHBCIPropetries(),plugin.getHBCICallback());
            
            continue; // ok, nochmal versuchen
          }
          catch (Exception e2)
          {
            Logger.error("unable to initialize HBCI4Java subsystem",e2);
          }
        }
        else
        {
          Logger.error("unable to verify account crc number",e);
        }
      }
    }
    
    Logger.error("unable to verify account crc number");
    return true;
  }
  
  /**
   * Prueft die Gueltigkeit einer IBAN anhand von Pruefziffern.
   * @see HBCIUtils#checkIBANCRC(java.lang.String)
   * @param iban die IBAN.
   * @return true, wenn die IBAN ok ist.
   * @deprecated Bitte {@link HBCIProperties#checkIBAN(String)} verwenden.
   */
  @Deprecated
  public final static boolean checkIBANCRC(String iban)
  {
    try
    {
      checkIBAN(iban);
      return true;
    }
    catch (ApplicationException ae)
    {
      return false;
    }
  }
  
  /**
   * Prueft die BIC und liefert eine ggf korrigierte Version zurueck. 
   * @param bic die zu pruefende BIC.
   * @return die korrigierte BIC (ggf um "XXX" ergaenzt).
   * @throws ApplicationException
   */
  public final static String checkBIC(String bic) throws ApplicationException
  {
    // Eine BIC hat entweder exakt 8 oder exakt 11 Zeichen. Bei 8 Zeichen vervollstaendigen
    // wir rechts mit 3 * X. Bei 11 Zeichen lassen wir es so. Bei irgendwas
    // anderem bringen wir einen Fehler.
    checkChars(bic,HBCI_BIC_VALIDCHARS);
    
    int len = bic.length();
    if (len != HBCI_BIC_MAXLENGTH && len != 8)
      throw new ApplicationException(i18n.tr("Bitte prüfen Sie die Länge der BIC. Muss entweder 8 oder 11 Zeichen lang sein."));
    
    if (len == 8)
      bic += "XXX";
    
    return bic;
  }
  
  /**
   * Prueft die Gueltigkeit einer Creditor-ID (Gläubiger-Identifikationsnummer)
   * anhand von Pruefziffern.
   * @see HBCIUtils#checkCredtitorIdCRC(String)
   * @param creditorId die Creditor-ID
   * @return true, wenn die Creditor-ID ok ist.
   */
  public final static boolean checkCreditorIdCRC(String creditorId)
  {
    try
    {
      if (creditorId == null || // Nichts angegeben
          creditorId.length() == 0 || // Nichts angegeben
          creditorId.length() > HBCI_SEPA_CREDITORID_MAXLENGTH ) // zu lang
      {
        return false;
      }
      return HBCIUtils.checkCredtitorIdCRC(creditorId);
    }
    catch (NumberFormatException nfe)
    {
      Logger.warn("invalid creditor-id: " + nfe.getMessage());
      return false;
    }
    catch (Exception e)
    {
      try
      {
        Logger.warn("HBCI4Java subsystem seems to be not initialized for this thread group, adding thread group");
        HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
        HBCIUtils.initThread(plugin.getHBCIPropetries(),plugin.getHBCICallback());
        return HBCIUtils.checkCredtitorIdCRC(creditorId);
      }
      catch (Exception e2)
      {
        Logger.error("unable to verify creditor id crc number",e2);
        return true;
      }
    }
  }

  /**
   * Erzeugt eine IBAN aus dem String und fuehrt diverse Pruefungen auf dieser durch.
   * @param iban die IBAN.
   * @return die gepruefte IBAN.
   * @throws ApplicationException die Fehlermeldung, wenn die IBAN nicht korrekt ist.
   */
  public final static IBAN getIBAN(String iban) throws ApplicationException
  {
    if (StringUtils.trimToNull(iban) == null)
      return null;
    
    iban = StringUtils.deleteWhitespace(iban);
    
    if (iban == null || iban.length() == 0)
      return null;
    
    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return null;

    try
    {
      return new IBAN(iban);
    }
    catch (SEPAException se)
    {
      Fehler f = se.getFehler();
      if (f != null && ignoredErrors.contains(f))
      {
        Logger.warn("unable to verify IBAN, got error " + f + ", will be tolerated");
        return null;
      }
      
      throw new ApplicationException(se.getMessage());
    }
  }


  /**
   * Prueft die IBAN auf Gueltigkeit.
   * @param iban die IBAN.
   * @throws ApplicationException die Fehlermeldung, wenn die IBAN nicht korrekt ist.
   */
  public final static void checkIBAN(String iban) throws ApplicationException
  {
    if (StringUtils.trimToNull(iban) == null)
      throw new ApplicationException(i18n.tr("Bitte geben Sie eine IBAN ein"));
    
    iban = StringUtils.deleteWhitespace(iban);
    
    if (iban == null || iban.length() == 0)
      throw new ApplicationException(i18n.tr("Bitte geben Sie eine IBAN ein"));

    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return;

    // Wenn die IBAN auch im Adressbuch steht, dann auch mit ungültiger Länge tolerieren
    if (de.willuhn.jameica.hbci.Settings.getKontoCheckExcludeAddressbook())
    {
      try
      {
        // OK, wir schauen im Adressbuch
        DBService db = de.willuhn.jameica.hbci.Settings.getDBService();
        HibiscusAddress address = (HibiscusAddress) db.createObject(HibiscusAddress.class,null);
        address.setIban(iban);
        AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
        if (service.contains(address) != null)
          return;
      }
      catch (Exception e)
      {
        Logger.error("unable to validate iban",e);
      }
    }

    try
    {
      final IBAN i = new IBAN(iban);
      
      // Rückgabe-Code checken
      IBANCode code = i.getCode();
      if (code == null || code == IBANCode.GUELTIG)
        return;
      
      // Tolerieren wir ebenfalls
      if (code == IBANCode.KONTONUMMERERSETZT || 
          code == IBANCode.GEMELDETEBLZZURLOESCHUNGVORGEMERKT ||
          code == IBANCode.PRUEFZIFFERNMETHODEFEHLT)
        return;

      throw new ApplicationException(code.getMessage());
    }
    catch (SEPAException e)
    {
      // Haben wir einen Fehlercode?
      Fehler f = e.getFehler();
      if (f != null)
      {
        String msg = obantooCodes.get(f);
        if (msg != null)
          throw new ApplicationException(i18n.tr("IBAN \"{0}\": {1}",iban,msg));
      }

      // Oder alternativ einen Fehlertext?
      String msg = e.getMessage();
      if (msg != null)
        throw new ApplicationException(i18n.tr("IBAN \"{0}\": {1}",iban,msg));
    }
    
    throw new ApplicationException(i18n.tr("IBAN ungültig: \"{0}\"",iban));
  }


  /**
   * Erzeugt die IBAN aus der uebergebenen Bankverbindung.
   * @param blz die BLZ.
   * @param konto die Kontonummer.
   * @return die IBAN.
   * @throws ApplicationException
   */
  public final static IBAN getIBAN(String blz, String konto) throws ApplicationException
  {
    try
    {
      IBAN iban = new IBAN(konto, blz, "DE");
      
      // Rückgabe-Code checken
      IBANCode code = iban.getCode();
      if (code == null || code == IBANCode.GUELTIG)
        return iban;
      
      // Tolerieren wir ebenfalls
      if (code == IBANCode.KONTONUMMERERSETZT || 
          code == IBANCode.GEMELDETEBLZZURLOESCHUNGVORGEMERKT ||
          code == IBANCode.PRUEFZIFFERNMETHODEFEHLT)
        return iban;

      // Fehler werfen
      throw new ApplicationException(code.getMessage());
    }
    catch (SEPAException e)
    {
      // Haben wir einen Fehlercode?
      Fehler f = e.getFehler();
      if (f != null)
      {
        String msg = obantooCodes.get(f);
        if (msg != null)
          throw new ApplicationException(msg);
      }

      // Oder alternativ einen Fehlertext?
      String msg = e.getMessage();
      if (msg != null)
        throw new ApplicationException(msg);

      Logger.error("unable to generate IBAN",e);
      throw new ApplicationException(i18n.tr("IBAN konnte nicht ermittelt werden"));
    }
    catch (Throwable e2) // BUGZILLA-1405 auch "ExceptionInInitializerError" in obantoo mit fangen
    {
      Logger.error("unable to generate IBAN",e2);
      throw new ApplicationException(i18n.tr("IBAN konnte nicht ermittelt werden"));
    }
  }
  
  /**
   * Laeuft den Stack der Exceptions bis zur urspruenglichen hoch und liefert sie zurueck.
   * HBCI4Java verpackt Exceptions oft tief ineinander. Sie werden gefangen, in eine
   * neue gepackt und wieder geworfen. Um nun die eigentliche Fehlermeldung zu kriegen,
   * suchen wir hier nach der ersten. 
   * BUGZILLA 249
   * @param t die Exception.
   * @return die urspruengliche.
   */
  public static Throwable getCause(Throwable t)
  {
    return getCause(t,null);
  }
  
  /**
   * Laeuft den Stack der Exceptions bis zur urspruenglichen hoch und liefert sie zurueck.
   * HBCI4Java verpackt Exceptions oft tief ineinander. Sie werden gefangen, in eine
   * neue gepackt und wieder geworfen. Um nun die eigentliche Fehlermeldung zu kriegen,
   * suchen wir hier nach der ersten. 
   * BUGZILLA 249
   * @param t die Exception.
   * @param c optionale Angabe der gesuchten Exception.
   * Wird sie nicht angegeben, liefert die Funktion die erste geworfene Exception
   * im Stacktrace. Wird sie angegeben, liefert die Funktion die erste gefundene
   * Exception dieser Klasse - insofern sie gefunden wird. Wird sie nicht gefunden,
   * liefert die Funktion NULL.
   * @return die urspruengliche.
   */
  public static Throwable getCause(Throwable t, Class<? extends Throwable> c)
  {
    Throwable cause = t;
    
    for (int i=0;i<20;++i) // maximal 20 Schritte nach oben
    {
      if (c != null && c.equals(cause.getClass()))
        return cause;
      
      Throwable current = cause.getCause();

      if (current == null)
        break; // Ende, hier kommt nichts mehr
      
      if (current == cause) // Wir wiederholen uns
        break;
      
      cause = current;
    }
    
    // Wenn eine gesuchte Exception angegeben wurde, haben wir sie hier nicht gefunden
    return c != null ? null : cause;
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
  
  /**
   * Ermittelt die Customer-IDs aus dem Passport.
   * @param passport Passport.
   * @return Liste der Customer-IDs.
   */
  public static Set<String> getCustomerIDs(HBCIPassport passport)
  {
    Konto[] accounts = passport.getAccounts();

    // Zum Vermeiden von Doppeln
    Set<String> set = new HashSet<String>();
    
    set.add(passport.getCustomerId()); // Die Customer-ID des Passport selbst auf jeden Fall auch

    // Das macht HBCI4Java in passport.getCustomerId() genauso
    // Wenn keine Customer-IDs vorhanden sind, wird die User-ID genommen
    if (accounts == null || accounts.length == 0)
    {
      set.add(passport.getUserId());
      return set;
    }

    // Und jetzt noch fuer die Kundenkennungen aller Konten.
    for (int i=0;i<accounts.length;++i)
    {
      String value = accounts[i].customerid;
      if (value != null)
        set.add(value);
    }
    return set;
  }

  // disabled
	private HBCIProperties()
	{
	}
	
}

