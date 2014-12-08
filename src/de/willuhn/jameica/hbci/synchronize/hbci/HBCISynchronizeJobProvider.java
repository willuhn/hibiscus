/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeJobProvider;


/**
 * Marker-Interface, um die eigenen Job-Provider zu finden.
 * Erweitert Comparable, um die Jobs sortieren zu koennen.
 */
public interface HBCISynchronizeJobProvider extends SynchronizeJobProvider
{
  /**
   * Liefert eine optionale Liste mit Property-Namen, die in Hibiscus
   * in den Sync-Einstellungen als Eingabefelder fuer zusaetzliche Konfigurationsoptionen
   * angezeigt werden sollen. Wird z.Bsp. vom ScriptingBackend verwendet, um dort
   * die Zugangsdaten zur Webseite hinterlegen zu koennen, ohne dafuer Kontonummer,
   * Benutzerkennung, usw. des Kontos "missbrauchen" zu muessen.
   * @param k das Konto.
   * @return Liste von lesbaren Property-Namen. Die werden dem Benutzer 1:1 als
   * Label von Eingabefeldern angezeigt.
   */
  public List<String> getPropertyNames(Konto k);
}
