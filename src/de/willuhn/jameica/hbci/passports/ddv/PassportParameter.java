/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.ddv;

import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader.Type;

/**
 * Haelt die Namen der HBCI4Java-Passport-Parameter.
 */
public class PassportParameter
{
  /**
   * Prefix fuer die Parameter.
   */
  private final static String PREFIX = "client.passport";

  /**
   * Parameter fuer den Port (meist 0)
   */
  public final static String PORT = "port";

  /**
   * Parameter fuer den Index (normalerweise 0)
   */
  public final static String CTNUMBER = "ctnumber";

  /**
   * Parameter ob die Tastatur zur Pin-Eingabe verwendet werden soll.
   */
  public final static String SOFTPIN  = "softpin";

  /**
   * Parameter fuer den Index des HBCI-Zugangs (meist 1).
   */
  public final static String ENTRYIDX = "entryidx";

  /**
   * Parameter fuer den Pfad und Dateinamen des CTAPI-Treibers.
   */
  public final static String CTAPI = "libname.ctapi";

  /**
   * Parameter fuer den Namen des Kartenlesers, wenn DDVPCSC (javax.smartcardio) verwendet wird.
   */
  public final static String NAME = "pcsc.name";

  /**
   * Liefert den Namen des Init-Parameters fuer HBCI4Java.
   * @param type der Kartenleser-Typ.
   * @param parameter der Name des Parameters.
   * @return der vollstaendige Parametername.
   */
  public static String get(Type type,String parameter)
  {
    return PREFIX + "." + type.getHeaderParam() + "." + parameter;
  }

}


