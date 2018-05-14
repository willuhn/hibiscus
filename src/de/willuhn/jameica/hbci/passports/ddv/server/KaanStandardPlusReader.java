/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv.server;


/**
 * Implementierung fuer die Default-Einstellungen des
 * "Kaan Standard Plus USB" von Kobil.
 */
public class KaanStandardPlusReader extends AbstractKaanReader
{
  /**
   * @see de.willuhn.jameica.hbci.passports.ddv.rmi.Reader#getName()
   */
  public String getName()
  {
    return "CTAPI: Kaan Standard Plus USB (Kobil)";
  }
}


/**********************************************************************
 * $Log: KaanStandardPlusReader.java,v $
 * Revision 1.2  2010/09/07 15:28:05  willuhn
 * @N BUGZILLA 391 - Kartenleser-Konfiguration komplett umgebaut. Damit lassen sich jetzt beliebig viele Kartenleser und Konfigurationen parellel einrichten
 *
 * Revision 1.1  2010/06/17 11:45:48  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/