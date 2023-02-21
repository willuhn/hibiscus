/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

/**
 * Implementierung des Schluesselformats im RAH10-Format.
 */
public class RAH10Format extends HBCI4JavaFormat
{
  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.HBCI4JavaFormat#getName()
   */
  public String getName()
  {
    return i18n.tr("HBCI4Java/Hibiscus-Format (RAH) (Commerzbank)");
  }
  
  /**
   * Liefert den Passport-Typ gemaess HBCI4Java.
   * @return Passport-Typ.
   */
  String getPassportType()
  {
    return "RAH10"; 
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.AbstractKeyFormat#getOrder()
   */
  @Override
  public int getOrder()
  {
    return 100;
  }
}
