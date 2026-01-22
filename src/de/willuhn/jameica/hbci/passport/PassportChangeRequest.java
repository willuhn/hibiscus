/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passport;

import org.hbci4java.hbci.passport.AbstractHBCIPassport;

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


