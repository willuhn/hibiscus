/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci;

/**
 * Bean, welche die Eckdaten des neuen PIN/TAN-Zugangs kapselt.
 */
public class HBCIAccountPinTan
{
  private String blz = null;
  private String url = null;
  private String customer = null;
  private String username = null;

  /**
   * Liefert die BLZ.
   * @return die BLZ.
   */
  public String getBlz()
  {
    return blz;
  }

  /**
   * Speichert die BLZ.
   * @param blz die BLZ.
   */
  public void setBlz(String blz)
  {
    this.blz = blz;
  }

  /**
   * Liefert die URL.
   * @return die URL.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Speichert die URL.
   * @param url die URL.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * Liefert die Kundenkennung.
   * @return customer die Kundenkennung.
   */
  public String getCustomer()
  {
    return customer;
  }

  /**
   * Speichert die Kundenkennung.
   * @param customer die Kundenkennung.
   */
  public void setCustomer(String customer)
  {
    this.customer = customer;
  }

  /**
   * Liefert den Usernamen.
   * @return der Username.
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Speichert den Usernamen.
   * @param username der Username.
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

}
