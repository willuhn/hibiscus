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
  @Override
  public String getName()
  {
    return i18n.tr("RAH10-Format (experimentell - derzeit ungetestet)");
  }
  
  @Override
  String getPassportType()
  {
    return "RAH10"; 
  }
  
  @Override
  public int getOrder()
  {
    return 100;
  }
}
