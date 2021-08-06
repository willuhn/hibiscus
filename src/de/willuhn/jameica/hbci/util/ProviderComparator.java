/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.util;

import de.willuhn.jameica.hbci.HasName;

import java.util.Comparator;
import java.util.Objects;

/**
 * Ein {@link Comparator}, um Provider (nach Namen) zu sortieren.
 *
 * <p>Hierbei wird ein Vergleichsobjekt mitgegeben, der Standardprovider, welches
 * je nach Wunsch entweder ganz vorn oder ganz hinten in der Aufzählung einsortiert wird.
 *
 * @param <T> Klasse der zu sortierenden Provider
 */
public class ProviderComparator<T extends HasName> implements Comparator<T>
{
  /** Standardprovider */
  private final Class<? extends T> PRIMARY;
  /** Gibt an, ob der Standardprovider ganz vorn oder ganz hinten einsortiert werden soll. */
  private final boolean PRIMARY_ZUERST;

  /**
   * Erzeugt einen {@link Comparator} zum Sortieren von Providern.
   *
   * @param primary Der Standardprovider.
   * @param alsErstes Bei {@code true} wird der Standardprovider ganz vorn einsortiert, bei {@code false} ganz
   *         hinten.
   */
  public ProviderComparator(final Class<? extends T> primary, boolean alsErstes)
  {
    PRIMARY = Objects.requireNonNull(primary);
    PRIMARY_ZUERST = alsErstes;
  }

  @Override
  public int compare(final T o1, final T o2)
  {
    if (PRIMARY.isInstance(o1))
      return PRIMARY_ZUERST ? -1 : 1;
    if (PRIMARY.isInstance(o2))
      return PRIMARY_ZUERST ? 1 : -1;
    // Ansonsten alphabetisch nach Name
    return o1.getName().compareTo(o2.getName());
  }
}
