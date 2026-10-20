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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testet die Klasse {@code StringUtils}.<br/>
 * Die erwarteten Werte entsprechen dem Verhalten
 * der bisher verwendeten Methoden aus commons-lang 2.6.
 */
final class TestStringUtils
{
  /**
   * Testet trimToEmpty und trimToNull.
   * @param text        die Eingabe.
   * @param trimToEmpty das erwartete Ergebnis von trimToEmpty.
   * @param trimToNull  das erwartete Ergebnis von trimToNull.
   */
  @ParameterizedTest
  @CsvSource({
    ",          '',  ",
    "'',        '',  ",
    "'   ',     '',  ",
    "'  a  ',   a,   a",
    "'\ta b\n', a b, a b",
  })
  void testTrim(String text, String trimToEmpty, String trimToNull)
  {
    assertEquals(trimToEmpty, StringUtils.trimToEmpty(text));
    assertEquals(trimToNull, StringUtils.trimToNull(text));
  }

  /**
   * Testet isBlank/isNotBlank und isEmpty/isNotEmpty.
   * @param text  die Eingabe.
   * @param blank das erwartete Ergebnis von isBlank.
   * @param empty das erwartete Ergebnis von isEmpty.
   */
  @ParameterizedTest
  @CsvSource({
    ",         true,  true",
    "'',       true,  true",
    "'   ',    true,  false",
    "'\t\n',   true,  false",
    "a,        false, false",
    "' a ',    false, false",
  })
  void testBlankAndEmpty(String text, boolean blank, boolean empty)
  {
    assertEquals(blank, StringUtils.isBlank(text));
    assertEquals(!blank, StringUtils.isNotBlank(text));
    assertEquals(empty, StringUtils.isEmpty(text));
    assertEquals(!empty, StringUtils.isNotEmpty(text));
  }

  /**
   * Testet deleteWhitespace.
   */
  @Test
  void testDeleteWhitespace()
  {
    assertNull(StringUtils.deleteWhitespace(null));
    assertEquals("", StringUtils.deleteWhitespace(""));
    assertEquals("", StringUtils.deleteWhitespace(" \t\n"));
    assertEquals("DE02120300000000202051", StringUtils.deleteWhitespace("DE02 1203 0000 0000 2020 51"));
    assertEquals("abc", StringUtils.deleteWhitespace("a\tb\nc"));

    // Ohne Whitespace kommt dieselbe Instanz zurück
    String text = "abc";
    assertSame(text, StringUtils.deleteWhitespace(text));
  }

  /**
   * Testet abbreviateMiddle.
   * @param text      die Eingabe.
   * @param middle    der Platzhalter für die Kürzungsstelle.
   * @param maxLength die maximale Länge.
   * @param expected  das erwartete Ergebnis.
   */
  @ParameterizedTest
  @CsvSource({
    "abc,     .,   0, abc",
    "abc,     .,   3, abc",
    "abcdef,  .,   4, ab.f",
    "abcdef,  .,   5, ab.ef",
    "abcdef,  .,   3, a.f",
    "abcdef,  .,   2, abcdef",
    "abcdef,  ..., 4, abcdef",
    "abcdef,  ,    4, abcdef",
    "abcdef,  '',  4, abcdef",
    "abcdefg, ..., 6, ab...g",
  })
  void testAbbreviateMiddle(String text, String middle, int maxLength, String expected)
  {
    assertEquals(expected, StringUtils.abbreviateMiddle(text, middle, maxLength));
  }

  /**
   * Testet abbreviateMiddle mit NULL.
   */
  @Test
  void testAbbreviateMiddleNull()
  {
    assertNull(StringUtils.abbreviateMiddle(null, ".", 4));
  }

  /**
   * Testet replaceEach.
   */
  @Test
  void testReplaceEach()
  {
    assertNull(StringUtils.replaceEach(null, new String[] { "a" }, new String[] { "b" }));
    assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[0]));
    assertEquals("zbz", StringUtils.replaceEach("aba", new String[] { "a" }, new String[] { "z" }));
    assertEquals("wcte", StringUtils.replaceEach("abcde", new String[] { "ab", "d" }, new String[] { "w", "t" }));

    // Paare mit leerem/NULL-Suchbegriff oder NULL-Ersetzung werden ignoriert
    assertEquals("abc", StringUtils.replaceEach("abc", new String[] { null, "", "a" }, new String[] { "x", "y", null }));
    assertEquals("xbc", StringUtils.replaceEach("abc", new String[] { "a" }, new String[] { "x" }));

    // Bereits ersetzter Text wird nicht erneut durchsucht
    assertEquals("zbz", StringUtils.replaceEach("aba", new String[] { "a", "z" }, new String[] { "z", "y" }));

    // NULL-Arrays oder leerer Text liefern den Text unverändert
    assertEquals("a", StringUtils.replaceEach("a", null, new String[] { "b" }));
    assertEquals("a", StringUtils.replaceEach("a", new String[] { "a" }, null));
    assertEquals("", StringUtils.replaceEach("", new String[] { "a" }, new String[0]));
  }

  /**
   * Testet, dass replaceEach bei unterschiedlich langen Arrays eine Exception wirft.
   */
  @Test
  void testReplaceEachLengthMismatch()
  {
    assertThrows(IllegalArgumentException.class, () -> StringUtils.replaceEach("a", new String[] { "a" }, new String[] { "b", "c" }));
  }

  /**
   * Testet randomAlphanumeric.
   * @param length die gewünschte Länge.
   */
  @ParameterizedTest
  @ValueSource(ints = {0, 1, 8, 64})
  void testRandomAlphanumeric(int length)
  {
    String s = StringUtils.randomAlphanumeric(length);
    assertEquals(length, s.length());
    assertTrue(s.matches("[A-Za-z0-9]*"), s);
  }

  /**
   * Testet, dass randomAlphanumeric bei negativer Länge eine Exception wirft.
   */
  @Test
  void testRandomAlphanumericNegative()
  {
    assertThrows(IllegalArgumentException.class, () -> StringUtils.randomAlphanumeric(-1));
  }

  /**
   * Testet, dass zwei Aufrufe von randomAlphanumeric unterschiedliche Werte liefern.
   */
  @Test
  void testRandomAlphanumericDiffers()
  {
    assertFalse(StringUtils.randomAlphanumeric(32).equals(StringUtils.randomAlphanumeric(32)));
  }

}
