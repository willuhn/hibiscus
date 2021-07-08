/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import org.kapott.hbci.passport.HBCIPassport;

/**
 * Erlaubt das verzoegerte Laden von Passports on-demand.
 */
public interface PassportLoader
{
  /**
   * Laedt den Passport.
   * Er darf intern gecached werden.
   * @return der Passport.
   */
  public HBCIPassport load();

  /**
   * Forciert ein Reload des Passports.
   */
  public void reload();

}
