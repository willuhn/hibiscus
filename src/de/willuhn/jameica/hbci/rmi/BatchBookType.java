/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;


/**
 * Die verschiedenen Varianten des Batch-Booking-Flags.
 */
public enum BatchBookType
{
  /**
   * BatchBooking-Flag gar nicht mitschicken.
   */
  NONE("","Vorgabewert der Bank verwenden (Standard)"),
  
  /**
   * BatchBooking-Flag als true senden.
   */
  TRUE("1","Als Stapelbuchung senden"),
  
  /**
   * BatchBooking-Flag als false senden.
   */
  FALSE("0","Als Einzelbuchungen senden (erfordert ggf. Bankvereinbarung)"),
  
  ;
  
  /**
   * Der Default-Typ (NONE).
   */
  public static BatchBookType DEFAULT = NONE;
  
  private String value = null;
  private String description = null;
  
  /**
   * ct.
   * @param value der zugehoerige Property-Wert.
   * @param description sprechender Name des Typs.
   */
  private BatchBookType(String value, String description)
  {
    this.value       = value;
    this.description = description;
  }
  
  /**
   * Liefert den zugehoerigen Property-Wert.
   * @return der zugehoerige Property-Wert.
   */
  public String getValue()
  {
    return this.value;
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Typ.
   * @return sprechender Name fuer den Typ.
   */
  public String getDescription()
  {
    return this.description;
  }
  
  /**
   * Liefert den passenden Batchbook-Typ fuer den angegebenen Wert.
   * @param value der Wert.
   * @return die zugehoerige Enum oder NULL, wenn der Typ nicht gefunden wurde.
   */
  public static BatchBookType byValue(String value)
  {
    if (value != null)
    {
      for (BatchBookType type:BatchBookType.values())
      {
        if (value.equals(type.value))
          return type;
      }
    }
    
    return null;
  }
  
  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return this.name() + ": " + i18n.tr(this.getDescription());
  }
}


