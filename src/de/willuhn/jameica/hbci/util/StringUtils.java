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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ersatz für die entsprechenden Methoden aus {@code org.apache.commons.lang.StringUtils},
 * die in Hibiscus bisher direkt verwendet wurden.
 *
 * @since 2.13.0
 */
public final class StringUtils
{
  private static final char[] ALPHANUMERIC_CHARS =
       ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      + "abcdefghijklmnopqrstuvwxyz"
      + "0123456789").toCharArray();

  /**
   * Privater Konstruktor.
   */
  private StringUtils()
  {
  }

  /**
   * Trimmt eine Zeichenkette, niemals {@code null}.
   * @param text die zu trimmende Zeichenkette, darf {@code null} sein.
   * @return die getrimmte Zeichenkette oder Leerstring, wenn text {@code null} ist.
   */
  public static String trimToEmpty(CharSequence text)
  {
    return text == null ? "" : text.toString().trim();
  }

  /**
   * Trimmt eine Zeichenkette, niemals Leerstring.
   * @param text die zu trimmende Zeichenkette, darf {@code null} sein.
   * @return die getrimmte Zeichenkette oder {@code null}, wenn text {@code null} oder nach dem Trimmen leer ist.
   */
  public static String trimToNull(CharSequence text)
  {
    String trimmed = trimToEmpty(text);
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Prüft, ob eine Zeichenkette {@code null}, leer oder nur aus Whitespace besteht.
   * @param text die zu prüfende Zeichenkette, darf {@code null} sein.
   * @return {@code true}, wenn text {@code null}, leer oder nur aus Whitespace besteht.
   * @see Character#isWhitespace(char)
   */
  public static boolean isBlank(CharSequence text)
  {
    if (text == null)
    {
      return true;
    }

    int len = text.length();
    for (int i = 0; i < len; i++)
    {
      if (!Character.isWhitespace(text.charAt(i)))
      {
        return false;
      }
    }

    return true;
  }

  /**
   * Prüft, ob eine Zeichenkette weder {@code null}, leer noch nur aus Whitespace besteht.
   * @param text die zu prüfende Zeichenkette, darf {@code null} sein.
   * @return {@code true}, wenn text weder {@code null}, leer noch nur aus Whitespace besteht.
   */
  public static boolean isNotBlank(CharSequence text)
  {
    return !isBlank(text);
  }

  /**
   * Prüft, ob eine Zeichenkette {@code null} oder leer ist.
   * @param text die zu prüfende Zeichenkette, darf {@code null} sein.
   * @return {@code true}, wenn text {@code null} oder leer ist.
   */
  public static boolean isEmpty(CharSequence text)
  {
    return text == null || text.toString().isEmpty();
  }

  /**
   * Prüft, ob eine Zeichenkette weder {@code null} noch leer ist.
   * @param text die zu prüfende Zeichenkette, darf {@code null} sein.
   * @return {@code true}, wenn text weder {@code null} noch leer ist.
   */
  public static boolean isNotEmpty(CharSequence text)
  {
    return !isEmpty(text);
  }

  /**
   * Entfernt alle Whitespaces aus einer Zeichenkette, auch mittendrin.
   * @param text die Zeichenkette, darf {@code null} sein.
   * @return die Zeichenkette ohne Whitespaces, oder {@code null}, wenn text {@code null} ist.
   */
  public static String deleteWhitespace(CharSequence text)
  {
    if (text == null)
    {
      return null;
    }

    int len = text.length();
    StringBuilder sb = new StringBuilder(len);

    for (int i = 0; i < len; i++)
    {
      char c = text.charAt(i);
      if (!Character.isWhitespace(c))
      {
        sb.append(c);
      }
    }

    return sb.length() == len ? text.toString() : sb.toString();
  }

  /**
   * Kürzt eine Zeichenkette in der Mitte ein, wenn sie länger als maxLength ist, und fügt an der
   * Kürzungsstelle "middle" ein. Hält dabei maxLength exakt ein (Textanteil vor und nach "middle" wird dafür
   * entsprechend verteilt). Ist middle leer oder bleibt neben middle nicht mindestens ein Zeichen auf jeder
   * Seite übrig, wird der Text unverändert zurückgegeben.
   * @param text      die zu kürzende Zeichenkette, darf {@code null} sein.
   * @param middle    die Zeichenkette, die an der Kürzungsstelle eingefügt wird.
   * @param maxLength die maximale Länge des Ergebnisses.
   * @return die ggf. gekürzte Zeichenkette, oder {@code null}, wenn text {@code null} ist.
   */
  public static String abbreviateMiddle(CharSequence text, CharSequence middle, int maxLength)
  {
    if (text == null)
    {
      return null;
    }
    if (isEmpty(middle) || maxLength >= text.length() || maxLength < middle.length() + 2)
    {
      return text.toString();
    }

    String s = text.toString();
    String m = middle.toString();
    int targetLength = maxLength - m.length();
    int startLength = targetLength / 2 + targetLength % 2;
    int endLength = targetLength / 2;

    return s.substring(0, startLength) + m + s.substring(s.length() - endLength);
  }

  /**
   * Ersetzt in einem einzigen Durchlauf jedes Vorkommen eines Suchbegriffs aus searchList durch das Element mit
   * demselben Index aus replacementList. Es wird dabei kein bereits ersetzter Text erneut durchsucht - anders als
   * bei verketteten String.replace()-Aufrufen können sich Ersetzungen also nicht gegenseitig beeinflussen.
   * Paare, deren Suchbegriff leer oder {@code null} bzw. deren Ersetzung {@code null} ist, werden ignoriert.
   * @param text            die zu durchsuchende Zeichenkette, darf {@code null} sein.
   * @param searchList      die Suchbegriffe, darf {@code null} sein.
   * @param replacementList die zugehörigen Ersetzungen, darf {@code null} sein.
   * @return die Zeichenkette mit den Ersetzungen, oder {@code null}, wenn text {@code null} ist.
   * @throws IllegalArgumentException wenn text und beide Arrays nicht leer sind und ihre Längen nicht übereinstimmen.
   */
  public static String replaceEach(CharSequence text, String[] searchList, String[] replacementList)
  {
    if (text == null)
    {
      return null;
    }
    if (text.length() == 0 || searchList == null || searchList.length == 0 || replacementList == null || replacementList.length == 0)
    {
      return text.toString();
    }
    if (searchList.length != replacementList.length)
    {
      throw new IllegalArgumentException(String.format("Länge der Such- und Ersetzungs-Arrays stimmt nicht überein: %d vs %d", searchList.length, replacementList.length));
    }

    String s = text.toString();
    StringBuilder result = new StringBuilder(s.length());
    int i = 0;
    outer:
    while (i < s.length())
    {
      for (int j = 0; j < searchList.length; ++j)
      {
        String search = searchList[j];
        String replacement = replacementList[j];
        if (search != null && !search.isEmpty() && replacement != null && s.startsWith(search, i))
        {
          result.append(replacement);
          i += search.length();
          continue outer;
        }
      }
      result.append(s.charAt(i));
      i++;
    }
    return result.toString();
  }

  /**
   * Erzeugt eine zufällige, rein alphanumerische Zeichenkette (Groß-/Kleinbuchstaben und Ziffern).
   * Nicht für sicherheitskritische Zwecke gedacht, nur z.B. für temporäre Dateinamen.
   * @param length die Länge der zu erzeugenden Zeichenkette, muss nicht-negativ sein.
   * @return die zufällige Zeichenkette.
   * @throws IllegalArgumentException falls {@code length} negativ ist.
   */
  public static String randomAlphanumeric(int length)
  {
    if (length < 0)
    {
      throw new IllegalArgumentException(String.format("Länge darf nicht negativ sein: %d", length));
    }

    char[] buffer = new char[length];
    ThreadLocalRandom rand = ThreadLocalRandom.current();
    for (int i = 0; i < length; i++)
    {
      buffer[i] = ALPHANUMERIC_CHARS[rand.nextInt(ALPHANUMERIC_CHARS.length)];
    }

    return new String(buffer);
  }
}
