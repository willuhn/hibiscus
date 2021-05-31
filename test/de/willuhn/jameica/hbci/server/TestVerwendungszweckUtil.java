/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.util.Map;

import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die Klasse "VerwendungszweckUtil".
 */
public class TestVerwendungszweckUtil
{
  /**
   * Testet die Funktion" rewrap".
   */
  @Nested
  class Rewrap
  {
    @Test
    void istNullmitParameterNull() // TODO besser IllegalArgumentException?
    {
      assertNull(VerwendungszweckUtil.rewrap(5, (String[]) null));
    }

    @Test
    void leeresArray()
    {
      String[] test = {};
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      assertArrayEquals(new String[]{}, result);
    }

    @Test
    void nurNullImArray() // TODO besser {} als Ergebnis?
    {
      String[] test = {
              null,
              null,
              null,
              null
      };
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      assertArrayEquals(test, result);
    }

    /**
     * Sonderfall mit ausschließlich leeren Teiltexten
     *
     * @see #wennZuKurzDannNichtsTun()
     */
    @Test
    void leereStrings() // TODO besser { "" } als Ergebnis?
    {
      String[] test = {"", "", "", "", "", "", "", ""};
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      assertArrayEquals(test, result);
    }

    @Test
    @DisplayName("Teiltext kürzer als Limit -> nichts tun")
    void wennZuKurzDannNichtsTun()
    {
      String[] test = {"a", "ab", "abc", "abcd", "abc def ghi jk"};
      String[] result = VerwendungszweckUtil.rewrap(15, test);
      assertArrayEquals(test, result);
    }

    @Test
    void nullImArrayIgnorieren()
    {
      String[] test = {
              "12345",
              "67890",
              null,
              "abcdef",
              "AB"
      };
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      String[] expected = {"123", "456", "789", "0ab", "cde", "fAB"};
      assertArrayEquals(expected, result);
    }

    @Test
    @DisplayName("Leerzeichen am Anfang und Ende der Teilstrings werden ignoriert, in der Mitte aber nicht")
    void leerzeichen()
    {
      String[] test = {
              "123 45   ",
              "6789  0  ",
              "  abcd ef",
              "       AB"
      };
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      String[] expected = {"123", " 45", "678", "9  ", "0ab", "cd ", "efA", "B"};
      assertArrayEquals(expected, result);
    }

    /**
     * @see #leerzeichen()
     * @see #leereStrings()
     */
    @Test
    void nurLeerzeichen() // TODO abweichend von den anderen Ergebnissen
    {
      String[] test = {"", " ", "  ", "    ", "     ", "      ", ""};
      String[] result = VerwendungszweckUtil.rewrap(3, test);
      String[] expected = {""};
      assertArrayEquals(expected, result);
    }

    @Test
    void limit1()
    {
      String[] test = {"Dies ", "ist", " ein Test."};
      String[] result = VerwendungszweckUtil.rewrap(1, test);
      String[] expected = {"D", "i", "e", "s", "i", "s", "t", "e", "i", "n", " ", "T", "e", "s", "t", "."};
      assertArrayEquals(expected, result);
    }

    @Test
    void limit0() // TODO besser IllegalArgumentException?
    {
      String[] test = {"Dies ", "ist", " ein Test."};
      String[] result = VerwendungszweckUtil.rewrap(0, test);
      String[] expected = {"", "D", "i", "e", "s", "i", "s", "t", "e", "i", "n", " ", "T", "e", "s", "t", "."};
      assertArrayEquals(expected, result);
    }

    @Test
    void limitNegativ() // TODO besser IllegalArgumentException mit besserem Hinweistext?
    {
      String[] test = {"Dies ", "ist", " ein Test."};
      // wirft java.util.regex.PatternSyntaxException, welche von java.lang.IllegalArgumentException abgeleitet ist
      assertThrows(IllegalArgumentException.class, () -> VerwendungszweckUtil.rewrap(-20, test));
    }

    @Test
    void limitMaxInt()
    {
      String[] test = {"Dies ", "ist", " ein Test."};
      String[] result = VerwendungszweckUtil.rewrap(Integer.MAX_VALUE, test);
      assertArrayEquals(test, result);
    }

    @Test
    void testRewrap()
    {
      String[] test = {
              "123456789012345678901234567890",
              "123456789012345678901234567890 ",
              null,
              "d",
              " ",
              "123456789012345678901234567"
      };

      String[] result = VerwendungszweckUtil.rewrap(27, test);
      String[] expected = {
              "123456789012345678901234567",
              "890123456789012345678901234",
              "567890d12345678901234567890",
              "1234567"
      };
      assertArrayEquals(expected, result);
    }
  }

  @Nested
  @DisplayName("Parsen der Tags aus String-Array")
  class ParseWithArray
  {
    @Test
    void parameterNull() throws Exception
    {
      String[] test = null;
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertNotNull(map);
      assertTrue(map.keySet().isEmpty());
    }

    @Test
    void leeresArray() throws Exception
    {
      String[] test = {};
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertNotNull(map);
      assertTrue(map.keySet().isEmpty());
    }

    @Test
    void nurNullImArray() throws Exception
    {
      String[] test = {
              null,
              null,
              null,
              null
      };
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertNotNull(map);
      assertTrue(map.keySet().isEmpty());
    }

    @Test
    void nurLeerImArray() throws Exception
    {
      String[] test = {"", " ", "  ", "    ", "     ", "      ", ""};
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertNotNull(map);
      assertTrue(map.keySet().isEmpty());
    }

    @Test
    @DisplayName("Text ganz ohne Tags")
    void ohneTags() throws Exception
    {
      String[] test = {"Dies ist ein Test."};
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertNotNull(map);
      assertTrue(map.keySet().isEmpty());
    }

    @Test
    void leererSvwz() throws Exception
    {
      String[] test = {"SVWZ+"};

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertEquals(1, map.keySet().size());
      assertTrue(map.containsKey(Tag.SVWZ));
      check(map, "SVWZ", "");
    }

    @Test
    void einzeiligerSvwz() throws Exception
    {
      String[] test = {"SVWZ+Nur eine Zeile"};
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Nur eine Zeile");
    }

    @Test
    void mehrzeiligerSvwz() throws Exception
    {
      // Entfernen von Zeilenumbruechen im Verwendungszweck
      String[] test = {
              "SVWZ+Das ist Zeile 1",
              "2",
              "3"
      };
      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Das ist Zeile 123");
    }

    @Test
    void ohneSvwzTagAberMitZweitemTagMitPlus() throws Exception
    {
      String[] test = {
              "Das ist eine Zeile ohne Tag ",
              "Fooo",
              "KREF+Und hier kommen ploetzlich noch Tags. Der Teil bis zum ersten Tag ist dann eigentlich der Verwendungszweck"
      };
      String kref = "Und hier kommen ploetzlich noch Tags. Der Teil bis zum ersten Tag ist dann eigentlich der Verwendungszweck";

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Das ist eine Zeile ohne Tag Fooo");
      check(map, "KREF", kref);
    }

    @Test
    void ohneSvwzTagAberMitZweitemTagMitDoppelpunkt() throws Exception
    {
      String[] test = {
              "Wir koennen auch",
              "mit",
              "KREF: Doppelpunkt als Separatur umgehen"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertEquals(2, map.keySet().size());
      assertTrue(map.containsKey(Tag.SVWZ));
      assertTrue(map.containsKey(Tag.KREF));
      check(map, Tag.SVWZ.name(), "Wir koennen auchmit");
      check(map, Tag.KREF.name(), "Doppelpunkt als Separatur umgehen");
    }

    @Test
    void einzeiligMitMehrerenTags() throws Exception
    {
      String[] test = {"IBAN: DE49390500000000012345 BIC: AACSDE33 ABWA: NetAachen"};

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "IBAN", "DE49390500000000012345");
      check(map, "BIC", "AACSDE33");
      check(map, "ABWA", "NetAachen");
    }

    @Test
    void sonderzeichen() throws Exception
    {
      String[] test = {
              "SVWZ+Ein komischer.Verwendungszweck ",
              "auf mehreren Zeilen ",
              "Hier kommt nochwas",
              "ABWA+Das ist ein..Test//Text ",
              "mit Zeilen-Umbruch"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Ein komischer.Verwendungszweck auf mehreren Zeilen Hier kommt nochwas");
      check(map, "ABWA", "Das ist ein..Test//Text mit Zeilen-Umbruch");
    }

    @Test
    void vermeindlicheTagsWerdenNichtAlsWerteErkannt() throws Exception
    {
      String[] test = {
              "SVWZ+Das folgende Tag gibts nicht",
              "Fooo",
              "ABCD+Gehoert zum Verwendungszweck"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      assertEquals(1, map.keySet().size());
      assertTrue(map.containsKey(Tag.byName("SVWZ")));
      assertNull(Tag.byName("ABCD")); // = existiert nicht
      assertFalse(map.containsKey(Tag.byName("ABCD")));
      check(map, "SVWZ", "Das folgende Tag gibts nichtFoooABCD+Gehoert zum Verwendungszweck");
    }

    @Test
    void trotzVermeindlicherTagsWerdenRichtigeTagsErkannt() throws Exception
    {
      String[] test = {
              "SVWZ+Das folgende Tag gibts nicht",
              "Fooo ",
              "ABCD+Gehoert zum Verwendungszweck",
              "EREF+Aber hier kommt noch was"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Das folgende Tag gibts nichtFooo ABCD+Gehoert zum Verwendungszweck");
      check(map, "EREF", "Aber hier kommt noch was");
    }

    @Test
    void leerzeichenHinterTagWerdenIgnoriert() throws Exception
    {
      String[] test = {
              "SVWZ+Das folgende Tag gibts nicht",
              "Fooo ",
              "ABCD+ Leerzeichen hinter dem Tag stoeren nicht",
              "KREF+ und hier stoeren sie auch nicht, sind aber nicht teil des Value "
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Das folgende Tag gibts nichtFooo ABCD+ Leerzeichen hinter dem Tag stoeren nicht");
      check(map, "KREF", "und hier stoeren sie auch nicht, sind aber nicht teil des Value");
    }

    @Test
    void gemischtePlusUndDoppelpunktWerdenErkannt() throws Exception
    // TODO hier Code von #parse() anpassen, um noch besser zu trennen? Was ist vom Anwender gewollt?
    {
      String[] test = {
              "SVWZ+ Das geht sogar",
              " gemischt ",
              "IBAN: DE1234567890 ",
              "BIC: AACSDE33"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      // ACHTUNG: durch die Mischung sind die Anteile mit Doppelpunkt im SVWZ enthalten!
      check(map, "SVWZ", "Das geht sogar gemischt IBAN: DE1234567890 BIC: AACSDE33");
      check(map, "IBAN", "DE1234567890");
      check(map, "BIC", "AACSDE33");
    }

    //region Sonderrolle IBAN und BIC

    /**
     * ehemals "testParse012"
     *
     * @see #gemischtePlusUndDoppelpunktWerdenErkannt()
     */
    @Test
    void beiMischungVonPlusUndDoppelpunktSindAllePunktTexteImTagMitPlusEnthalten() throws Exception
    {
      // TODO: hier Code von #parse() anpassen, um noch besser zu trennen? Was ist vom Anwender gewollt?
      // Aus Code:
      // Sonderrolle IBAN. Wir entfernen alles bis zum ersten Leerzeichen. Siehe "testParse012". Da hinter der
      // IBAN kein vernuenftiges Tag mehr kommt, wuerde sonst der ganze Rest da mit reinfallen. Aber nur, wenn
      // es erst nach 22 Zeichen kommt. Sonst steht es mitten in der IBAN drin. In dem Fall entfernen wir die
      // Leerzeichen aus der IBAN (siehe "testParse013")
      // TODO: Ist IBAN per Definition stets das letzte Tag? ("da hinter der IBAN kein vernünftiges Tag mehr kommt")
      String[] test = {
              "SVWZ+BIC:GENODED1SAM ",
              "IBAN:DE12345678901234567890 ",
              "Datum: 14.01.16 Zeit: 08:00 ",
              "KD 00012345 TAN 12345 ",
              "Beleg 12345  Kunde 12345"
      };
      // ACHTUNG: durch die Mischung sind die Anteile mit Doppelpunkt im SVWZ enthalten!
      String svwz = "BIC:GENODED1SAM IBAN:DE12345678901234567890 Datum: 14.01.16 Zeit: 08:00 KD 00012345 TAN 12345 Beleg 12345  Kunde 12345";

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "IBAN", "DE12345678901234567890");
      check(map, "BIC", "GENODED1SAM");
      check(map, "SVWZ", svwz);
    }

    @Test
    void leerzeichenAusBicEntfernen() throws Exception
    {
      String[] test = {"BIC: G E N O D E D 1 S A M   "};
      check(VerwendungszweckUtil.parse(test), "BIC", "GENODED1SAM");

      String[] test2 = {"BIC+ ", " CO", "BA ", "   DE", " FFXXX "};
      check(VerwendungszweckUtil.parse(test2), "BIC", "COBADEFFXXX");
    }

    @Test
    void leerzeichenAusIbanEntfernen() throws Exception
    {
      String[] test = {"IBAN:  DE1234 56789012 34567890  "};
      check(VerwendungszweckUtil.parse(test), "IBAN", "DE12345678901234567890");

      String[] test2 = {"IBAN+ ", " DE09", "  8765", "4321 ", "0987", "6543", "    21"};
      check(VerwendungszweckUtil.parse(test2), "IBAN", "DE09876543210987654321");
    }

    /**
     * ehemals "testParse013"
     *
     * @see #leerzeichenAusBicEntfernen()
     * @see #leerzeichenAusIbanEntfernen()
     */
    @Test
    void umbruchBeiBicUndIban() throws Exception
    {
      String[] test = {
              "Verwendungszweck EREF: 12 34",
              "567890 123456789 IBAN: DE123",
              "45678901234567890 BIC: ABCD",
              "EFGH"
      };

      Map<Tag, String> map = VerwendungszweckUtil.parse(test);
      check(map, "SVWZ", "Verwendungszweck");
      check(map, "EREF", "12 34567890 123456789"); // Hier bleibt das Leerzeichen drin, da wir nicht wissen, ob es drin sein darf
      check(map, "IBAN", "DE12345678901234567890"); // Hier muss das Leerzeichen raus
      check(map, "BIC", "ABCDEFGH"); // Hier auch ohne Leerzeichen
    }
    //endregion

    private void check(Map<Tag, String> map, String tag, String expected)
    {
      assertEquals(expected, map.get(Tag.byName(tag)), tag);
    }
  }
}


