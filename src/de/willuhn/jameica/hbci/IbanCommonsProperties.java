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

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;

import de.speedbanking.checkdigit.de.CheckDigitResult;
import de.speedbanking.checkdigit.de.GermanAccountCheckDigit;
import de.speedbanking.iban.Iban;
import de.speedbanking.iban.IbanRegistry;
import de.speedbanking.iban.IbanValidationError;
import de.speedbanking.iban.InvalidIbanException;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * IBAN-Pruefungen/-Erzeugung auf Basis von "iban-commons"/"iban-commons-de-checkdigit"
 * anstelle von OBanToo.
 * <p>
 * Diese Klasse spiegelt bewusst nur die drei OBanToo-basierten Methoden aus
 * {@link HBCIProperties} (Verhaltensparitaet, siehe dortige Javadoc), ruehrt aber
 * an {@link HBCIProperties} selbst noch nichts an. Die Umstellung der Aufrufer
 * erfolgt erst, nachdem beide Implementierungen gegeneinander cross-geprueft wurden.
 * <p>
 * Bekannte Verhaltens-Luecke gegenueber OBanToo: OBanToo kennt ueber
 * {@code IBANCode.KONTONUMMERERSETZT} und {@code IBANCode.GEMELDETEBLZZURLOESCHUNGVORGEMERKT}
 * BLZ-Datei-Flags fuer Bankfusionen/BLZ-Loeschung. HBCI4Java's {@link BankInfo} bietet dafuer
 * kein Aequivalent - betroffene BLZ fallen ersatzlos unter die allgemeine
 * "Pruefziffern-Verfahren unbekannt"-Toleranz.
 */
public class IbanCommonsProperties
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Buendelt IBAN und BIC, das Ergebnis von {@link #getIBAN(String, String)}.
   */
  public static final class IbanAndBic
  {
    private final String iban;
    private final String bic;
    private final boolean checked;

    private IbanAndBic(String iban, String bic, boolean checked)
    {
      this.iban = iban;
      this.bic  = bic;
      this.checked = checked;
    }

    /**
     * Liefert die IBAN.
     * @return die IBAN.
     */
    public String getIban()
    {
      return this.iban;
    }

    /**
     * Liefert die BIC.
     * @return die BIC. Kann NULL sein, wenn sie nicht ermittelbar war.
     */
    public String getBic()
    {
      return this.bic;
    }

    /**
     * Ob die Kontonummer-Pruefziffer tatsaechlich kontrolliert werden konnte.
     * @return true, wenn ein bekanntes Pruefzifferverfahren angewendet wurde. false, wenn das
     * Verfahren fuer diese BLZ unbekannt oder in iban-commons-de-checkdigit nicht implementiert
     * ist - die IBAN wurde dann zwar strukturell erzeugt, die Pruefziffer aber nicht kontrolliert.
     */
    public boolean isChecked()
    {
      return this.checked;
    }
  }

  /**
   * Prueft die IBAN auf Gueltigkeit.
   * @param iban die IBAN.
   * @throws ApplicationException die Fehlermeldung, wenn die IBAN nicht korrekt ist.
   * @see HBCIProperties#checkIBAN(String)
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

    // Wenn die IBAN auch im Adressbuch steht, dann auch mit ungueltiger Laenge tolerieren
    if (de.willuhn.jameica.hbci.Settings.getKontoCheckExcludeAddressbook())
    {
      try
      {
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
      Iban i = Iban.of(iban);

      if ("DE".equals(i.getCountryCode()))
        checkGermanNationalCheckDigit(i.getBankCode(), i.getAccountNumber(), iban);
    }
    catch (InvalidIbanException e)
    {
      IbanValidationError reason = e.getReason();

      // Tolerieren wir, analog zum bisherigen IBANCode.UNGUELTIGESLAND
      if (reason == IbanValidationError.INVALID_COUNTRY)
        return;

      throw new ApplicationException(i18n.tr("IBAN \"{0}\": {1}",iban,message(reason)));
    }
  }

  /**
   * Erzeugt eine IBAN aus dem String und fuehrt diverse Pruefungen auf dieser durch.
   * @param iban die IBAN.
   * @return die gepruefte IBAN.
   * @throws ApplicationException die Fehlermeldung, wenn die IBAN nicht korrekt ist.
   * @see HBCIProperties#getIBAN(String)
   */
  public final static Iban getIBAN(String iban) throws ApplicationException
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
      Iban i = Iban.of(iban);

      if ("DE".equals(i.getCountryCode()))
        checkGermanNationalCheckDigit(i.getBankCode(), i.getAccountNumber(), iban);

      return i;
    }
    catch (InvalidIbanException e)
    {
      IbanValidationError reason = e.getReason();

      // Tolerieren wir, analog zum bisherigen ignoredErrors={UNGUELTIGESLAND}
      if (reason == IbanValidationError.INVALID_COUNTRY)
        return null;

      throw new ApplicationException(i18n.tr("IBAN \"{0}\": {1}",iban,message(reason)));
    }
  }

  /**
   * Erzeugt die IBAN aus der uebergebenen Bankverbindung.
   * @param blz die BLZ.
   * @param konto die Kontonummer.
   * @return die IBAN und die zugehoerige BIC.
   * @throws ApplicationException die Fehlermeldung, wenn die BLZ/Kontonummer-Kombi nicht korrekt ist.
   * @see HBCIProperties#getIBAN(String, String)
   */
  public final static IbanAndBic getIBAN(String blz, String konto) throws ApplicationException
  {
    Iban iban = IbanRegistry.DE.builder()
      .bankCode(blz)
      .accountNumber(konto)
      .build();

    boolean checked = checkGermanNationalCheckDigit(blz, konto, iban.toString());

    return new IbanAndBic(iban.toString(), HBCIUtils.getBICForBLZ(blz), checked);
  }

  /**
   * Prueft die deutsche Kontonummer-Pruefziffer per {@link GermanAccountCheckDigit}, sofern
   * fuer die BLZ ein Pruefzifferverfahren bekannt ist. Ist das Verfahren unbekannt oder in
   * iban-commons-de-checkdigit nicht implementiert, wird das toleriert (siehe Klassen-Javadoc).
   * @param blz die BLZ.
   * @param konto die Kontonummer.
   * @param iban die zugehoerige IBAN, nur fuer die Fehlermeldung.
   * @return true, wenn die Pruefziffer tatsaechlich kontrolliert werden konnte; false, wenn das
   * Verfahren unbekannt oder nicht implementiert ist und deshalb toleriert wurde.
   * @throws ApplicationException wenn die Pruefziffer nachweislich falsch ist.
   */
  private static boolean checkGermanNationalCheckDigit(String blz, String konto, String iban) throws ApplicationException
  {
    BankInfo info = HBCIUtils.getBankInfo(blz);
    String method = info != null ? StringUtils.trimToNull(info.getChecksumMethod()) : null;

    if (method == null)
      return false; // Verfahren unbekannt - wie bisher IBANCode.PRUEFZIFFERNMETHODEFEHLT tolerieren

    CheckDigitResult result;
    try
    {
      result = GermanAccountCheckDigit.verify(method,blz,konto);
    }
    catch (IllegalArgumentException e)
    {
      // Verfahren in iban-commons-de-checkdigit nicht implementiert - ebenfalls tolerieren
      Logger.warn("unable to verify check digit, method " + method + " not implemented, will be tolerated");
      return false;
    }

    if (result.isChecked() && !result.isValid())
      throw new ApplicationException(i18n.tr("IBAN \"{0}\": {1}",iban,i18n.tr("Pr\u00fcfziffer der Kontonummer falsch")));

    return result.isChecked();
  }

  /**
   * Liefert den deutschen Fehlertext zu einem IBAN-Validierungsfehler.
   * @param reason der Fehlergrund.
   * @return der Fehlertext.
   */
  private static String message(IbanValidationError reason)
  {
    switch (reason)
    {
      case EMPTY:
        return i18n.tr("Bitte geben Sie eine IBAN ein");
      case INCORRECT_LENGTH:
      case INCORRECT_LENGTH_COUNTRY:
        return i18n.tr("L\u00e4nge der Kontonummer ung\u00fcltig");
      case INVALID_CHECK_DIGITS:
      case INVALID_CHECKSUM:
        return i18n.tr("Pr\u00fcfziffer der Kontonummer falsch");
      case INVALID_COUNTRY:
        return i18n.tr("Land unbekannt");
      default:
        return i18n.tr("IBAN-Regel unbekannt");
    }
  }

  // disabled
  private IbanCommonsProperties()
  {
  }

}
