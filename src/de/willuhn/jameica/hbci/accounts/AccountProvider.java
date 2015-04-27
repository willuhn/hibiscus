/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts;

/**
 * Interface fuer einen Account-Provider.
 * Wird typischerweise einmal pro SynchronizeBackend implementiert.
 */
public interface AccountProvider
{
  /**
   * Liefert einen sprechenden Namen fuer den Provider.
   * @return sprechender Name fuer den Provider.
   */
  public String getName();
  
  /**
   * Liefert einen Beschreibungstext fuer den Provider.
   * @return Beschreibungstext.
   */
  public String getDescription();
  
  /**
   * Liefert ein Icon fuer den Provider.
   * @return Icon.
   */
  public String getIcon();
  

}


