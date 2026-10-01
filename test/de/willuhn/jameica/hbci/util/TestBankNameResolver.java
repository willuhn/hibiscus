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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testet die Klasse "BankNameResolver".
 */
final class TestBankNameResolver
{
  /**
   * Testet das Lookup per (deutscher) BLZ.
   */
  @Test
  void testByBlz()
  {
    assertEquals("Bundesbank",BankNameResolver.getNameForBank("10000000"));
    assertEquals("Commerzbank",BankNameResolver.getNameForBank("37040044"));
  }

  /**
   * Testet das Lookup per BIC.
   */
  @Test
  void testByBic()
  {
    assertEquals("Bundesbank",BankNameResolver.getNameForBank("MARKDEF1100"));
    assertEquals("Hausbank München",BankNameResolver.getNameForBank("GENODEF1M04"));
  }

  /**
   * Testet, dass eine unbekannte BLZ/BIC NULL liefert statt eine Exception zu werfen.
   */
  @Test
  void testUnknown()
  {
    assertNull(BankNameResolver.getNameForBank("99999999"));
    assertNull(BankNameResolver.getNameForBank("XXXXXXXXXXX"));
  }

  /**
   * Testet die Behandlung von NULL/Leerstring/Whitespace.
   */
  @Test
  void testNullOrEmpty()
  {
    assertNull(BankNameResolver.getNameForBank(null));
    assertNull(BankNameResolver.getNameForBank(""));
    assertNull(BankNameResolver.getNameForBank("   "));
  }

  /**
   * Testet, dass zu lange Banknamen auf {@link BankNameResolver#MAX_NAME_LENGTH} gekürzt werden.
   */
  @Test
  void testLongNameIsAbbreviated()
  {
    // BLZ 60050101: "Landesbank Baden-Württemberg/Baden-Württembergische Bank" (56 Zeichen)
    String name = BankNameResolver.getNameForBank("60050101");
    assertEquals("Landesbank ...ische Bank",name);
    assertEquals(BankNameResolver.MAX_NAME_LENGTH,name.length());
  }
}
