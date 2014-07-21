package de.willuhn.jameica.hbci.passports.securepwd.rmi;

import de.willuhn.jameica.hbci.passports.securepwd.server.SecurePwdCfgImpl;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Interface für die PassportKlasse für Plugins
 * @author sven
 *
 */
public interface Passport extends de.willuhn.jameica.hbci.passport.Passport
{

  /**
   * Liefert passend zur gesetzen Klasse, Konto und Beschreibung eine Konfiguration zurück
   * @return Configuration (null, falls noch nicht vorhanden)
   * @throws Exception
   */
  SecurePwdCfgImpl getConfig() throws Exception;

  /**
   * Erzeugt passend zur gesetzen Klasse, Konto und Beschreibung eine Konfiguration und liefert diese zurück
   * @return Configuration
   * @throws Exception
   */
  SecurePwdCfgImpl createConfig() throws Exception;

  /**
   * Initalisiziert die Klasse
   * @param k Konto
   * @param pluginclass Plugin-Klasse
   * @param description Beschreibung (Wird zur Darstellung der Zugänge genutzt)
   */
  void init(Konto k, Class pluginclass, String description);
  
}