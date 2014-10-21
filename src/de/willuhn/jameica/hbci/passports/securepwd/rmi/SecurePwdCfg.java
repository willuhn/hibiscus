package de.willuhn.jameica.hbci.passports.securepwd.rmi;

import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.util.ApplicationException;

/**
 * Configuration für Plugin-Pwd-Verwaltung
 * @author sven
 *
 */
public interface SecurePwdCfg extends Configuration
{

  /**
   * 
   * Liefert zu einem Attribute das Value aus dem Wallet zurück
   * 
   * @param attribute Attributsnamen
   * @return Value
   * @throws ApplicationException
   */
  String getString(String attribute) throws ApplicationException;

  /**
   * 
   * Schreibt zu einem Attribute den Wert in das Wallet
   * @param attribute Attributsnamen
   * @param value Wert
   * @throws ApplicationException
   */
  void setString(String attribute, String value) throws ApplicationException;

}
