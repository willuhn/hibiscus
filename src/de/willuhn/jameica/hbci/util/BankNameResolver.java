/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.util;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import de.speedbanking.bankdata.BankData;
import de.speedbanking.bankdata.BankDataLookup;

/**
 * Ermittelt zu einer BIC oder (deutschen) BLZ den Namen der Bank über iban-commons-bankdata.
 * Framework-unabhängig gehalten (keine Jameica-Laufzeitabhängigkeit), damit die Mapping-Logik
 * isoliert unit-testbar bleibt.
 *
 * @since 2.13.0
 */
public class BankNameResolver
{
  /**
   * Länge einer deutschen BLZ.
   */
  public static final int BLZ_LENGTH = 8;

  /**
   * Maximale Länge des zurückgelieferten Banknamens.
   */
  public static final int MAX_NAME_LENGTH = 24;

  /**
   * Ermittelt zu einer BIC oder BLZ den Namen der Bank.
   * @param bic die BIC oder BLZ.
   * @return der Name der Bank oder NULL, wenn nicht ermittelbar.
   */
  public final static String getNameForBank(String bic)
  {
    bic = StringUtils.trimToNull(bic);
    if (bic == null)
      return null;

    // Wenn sie 8 Zeichen lang ist, gehen wir davon aus, dass es eine (deutsche) BLZ ist.
    // Sonst versuchen wir es als BIC zu interpretieren.
    Optional<BankData> bankData = bic.length() == BLZ_LENGTH ?
      BankDataLookup.byBankCode("DE",bic) :
      BankDataLookup.byBic(bic);

    if (!bankData.isPresent())
      return null;

    // Text einkürzen, wenn er zu lang ist.
    // Normalerweise nicht nötig. Es gibt aber einige Banken, die z.Bsp. folgenden
    // Namen haben: "Landesbank Baden-Württemberg/Baden-Württembergische Bank"
    // Das verzerrt sonst die Layouts an einigen Stellen
    return StringUtils.abbreviateMiddle(bankData.get().getBankName(),"...",MAX_NAME_LENGTH);
  }

  // disabled
  private BankNameResolver()
  {
  }
}
