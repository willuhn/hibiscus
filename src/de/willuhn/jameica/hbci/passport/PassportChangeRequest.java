/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passport;

import org.kapott.hbci.passport.AbstractHBCIPassport;

/**
 * Container, der die Kunden- und User-Daten-Aenderungen fuer einen Passport haelt.
 */
public class PassportChangeRequest
{
  /**
   * Der zu aendernde Passport.
   */
  public AbstractHBCIPassport passport = null;
  
  /**
   * Die neue Kundenkennung.
   */
  public String custId = null;
  
  /**
   * Die neue Benutzerkennung.
   */
  public String userId = null;
  
  /**
   * ct.
   * @param passport der zu aendernde Passport.
   * @param custId die neue Kundenkennung.
   * @param userId die neue Benutzerkennung.
   */
  public PassportChangeRequest(AbstractHBCIPassport passport, String custId, String userId)
  {
    this.passport = passport;
    this.custId = custId;
    this.userId = userId;
  }
}


