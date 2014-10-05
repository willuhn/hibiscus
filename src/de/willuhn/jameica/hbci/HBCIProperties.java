/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCIProperties.java,v $
 * $Revision: 1.46 $
 * $Date: 2011/05/27 11:33:23 $
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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.HBCIUtils;

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
	public final static String HBCI_DTAUS_VALIDCHARS = settings.getString("hbci.dtaus.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.&-+*%/$�������"); 

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
  public final static String HBCI_SEPA_VALIDCHARS = settings.getString("hbci.sepa.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789':?,- (+.)/");

  /**
   * Liste der in SEPA erlaubten Zeichen. Jedoch erweitert um die im Inland extra erlaubten Zeichen - insbesondere die Umlaute.
   * Siehe Anlage3_Datenformate_V2.7.pdf Seite 23
   */
  public final static String HBCI_SEPA_VALIDCHARS_RELAX = HBCI_SEPA_VALIDCHARS + settings.getString("hbci.sepa.validchars.add", "�������&*$%");

  /**
   * Liste der fuer die Mandate-ID gueltigen Zeichen. RestrictedIdentificationSEPA2.
   */
  public final static String HBCI_SEPA_MANDATE_VALIDCHARS = settings.getString("hbci.sepa.mandate.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789':?,-(+.)/");

  /**
   * Liste der in einer IBAN erlaubten Zeichen.
   */
  public final static String HBCI_IBAN_VALIDCHARS = settings.getString("hbci.iban.validchars", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

  /**
   * Liste der in einer BIC erlaubten Zeichen.
   */
  public final static String HBCI_BIC_VALIDCHARS = settings.getString("hbci.bic.validchars", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"); 

	/**
   * Liste der in Bankleitzahlen erlaubten Zeichen.
   */
  public final static String HBCI_BLZ_VALIDCHARS = settings.getString("hbci.blz.validchars","0123456789"); 

  /**
   * Liste der in der BZ�-Pruefziffer erlaubten Zeichen.
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
   * Laenge der Pruefziffern bei BZ�-Ueberweisung.
   */
  public final static int HBCI_TRANSFER_BZU_LENGTH = settings.getInt("hbci.transfer.bzu.length",13);

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
   * Maximale Laenge der EndtoEnd-ID bei SEPA.
   */
  public final static int HBCI_SEPA_ENDTOENDID_MAXLENGTH = settings.getInt("hbci.sepa.endtoendid.maxlength",35);
  
  /**
   * Maximale Laenge der Mandate-ID bei SEPA.
   */
  public final static int HBCI_SEPA_MANDATEID_MAXLENGTH = settings.getInt("hbci.sepa.mandateid.maxlength",35);

  /**
   * Maximale Laenge der Glaeubiger-ID bei SEPA.
   */
  public final static int HBCI_SEPA_CREDITORID_MAXLENGTH = settings.getInt("hbci.sepa.creditorid.maxlength",35);

  /**
   * Text-Replacements fuer SEPA.
   * Die in SEPA nicht zulaessigen Zeichen "&*%$�������" werden ersetzt.
   */
  public final static String[][] TEXT_REPLACEMENTS_SEPA = new String[][] {new String[]{"&","*","%","$","�", "�", "�", "�", "�", "�", "�"},
                                                                          new String[]{"+",".",".",".","ue","oe","ae","Ue","Oe","Ae","ss"}};

  private final static Map<Fehler,String> obantooCodes = new HashMap<Fehler,String>()
  {{
    put(Fehler.BLZ_LEER,                                    i18n.tr("Keine BLZ angegeben"));
    put(Fehler.BLZ_UNGUELTIGE_LAENGE,                       i18n.tr("BLZ nicht achtstellig"));
    put(Fehler.BLZ_UNGUELTIG,                               i18n.tr("BLZ unbekannt"));
    put(Fehler.KONTO_LEER,                                  i18n.tr("Keine Kontonummer angegeben"));
    put(Fehler.KONTO_UNGUELTIGE_LAENGE,                     i18n.tr("L�nge der Kontonummer ung�ltig"));
    put(Fehler.KONTO_PRUEFZIFFER_FALSCH,                    i18n.tr("Pr�fziffer der Kontonummer falsch"));
    put(Fehler.KONTO_PRUEFZIFFERNREGEL_NICHT_IMPLEMENTIERT, i18n.tr("Pr�fziffern-Verfahren der Kontonummer unbekannt"));
    put(Fehler.IBANREGEL_NICHT_IMPLEMENTIERT,               i18n.tr("IBAN-Regel unbekannt"));
    put(Fehler.UNGUELTIGES_LAND,                            i18n.tr("Land unbekannt"));
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
    // In der deutschen Sprache gibt es keinen Grossbuchstaben von "�".
    // Wird nun ein Text von Java in Grossbuchstaben umgewandelt (mittels String#toUpperCase())
    // bleibt nicht etwa das "�" erhalten. Nein, es wird gegen "SS" ersetzt.
    // Haben wir nun einen String, der exakt maxLength lang ist und enth�lt er
    // ein "�" wuerden wir das hier tolerieren, bei der Ausfuehrung des
    // Geschaeftsvorfalls wuerde es jedoch zu einem Fehler kommen, da dort
    // der Text automatisch in Grossbuchstaben umgewandelt wird (geschieht
    // bei HBCI generell), damit das "�" gegen "SS" ersetzt wird und der
    // Text am Ende genau um ein Zeichen zu lang wird. Verrueckt, oder? ;)
    // Da ggf. auch mehrere "�" enthalten sind, ersetzen wir alle und schauen
    // dann, wie lang der Text geworden ist.
    if (chars.indexOf("�") != -1)
    {
      String s = chars.replaceAll("�","ss");
      if (s.length() > maxLength)
        throw new ApplicationException(i18n.tr("Der Text \"{0}\" wird nach der HBCI-Kodierung (� wird hierbei gegen SS ersetzt) zu lang.",chars));
    }
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
    return StringUtils.capitalize(group(s,4," "));
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
    
    return bank != null ? bank.getBezeichnung() : null;
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
   * @deprecated Bitte {@link HBCIProperties#getIBAN(String)} verwenden.
   */
  @Deprecated
  public final static boolean checkIBANCRC(String iban)
  {
    try
    {
      getIBAN(iban);
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
      throw new ApplicationException(i18n.tr("Bitte pr�fen Sie die L�nge der BIC. Muss entweder 8 oder 11 Zeichen lang sein."));
    
    if (len == 8)
      bic += "XXX";
    
    return bic;
  }
  /**
   * Prueft die Gueltigkeit einer Creditor-ID (Gl�ubiger-Identifikationsnummer)
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
    if (!de.willuhn.jameica.hbci.Settings.getKontoCheck())
      return null;
    
    try
    {
      return new IBAN(iban);
    }
    catch (SEPAException se)
    {
      throw new ApplicationException(se.getMessage());
    }
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
      
      // R�ckgabe-Code checken
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
      // Checken, ob wir einen Fehlertext haben
      String msg = e.getMessage();
      if (msg != null)
        throw new ApplicationException(msg);
      
      // Dann halt anhand des Fehlercodes
      Fehler f = e.getFehler();
      if (f != null)
      {
        msg = obantooCodes.get(f);
        if (msg != null)
          throw new ApplicationException(msg);
      }
      
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

  // disabled
	private HBCIProperties()
	{
	}

}

