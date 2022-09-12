/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
  NONE(null,"","nach Vorgabewert der Bank ausweisen (Standard)"),
  
  /**
   * BatchBooking-Flag als true senden.
   */
  TRUE(Boolean.TRUE,"1","als Sammelbuchung ausweisen"),
  
  /**
   * BatchBooking-Flag als false senden.
   */
  FALSE(Boolean.FALSE,"0","als Einzelbuchungen ausweisen (erfordert ggf. Bankvereinbarung)"),
  
  ;
  
  /**
   * Der Default-Typ (NONE).
   */
  public static BatchBookType DEFAULT = NONE;
  
  private Boolean bv = null;
  private String value = null;
  private String description = null;
  
  /**
   * ct.
   * @param bv der interne Boolean-Wert.
   * @param value der zugehoerige Property-Wert, welcher auch von HBCI4Java verwendet wird.
   * @param description sprechender Name des Typs.
   */
  private BatchBookType(Boolean bv, String value, String description)
  {
    this.bv          = bv;
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
   * Liefert den zugehoerigen Boolean-Wert.
   * @return der zugehoerige Boolean-Wert.
   */
  public Boolean getBooleanValue()
  {
    return this.bv;
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
   * Liefert den passenden Batchbook-Typ fuer den angegebenen Wert wie er in der XML-Datei steht.
   * @param value der Wert.
   * @return die zugehoerige Enum. Niemals NULL sondern in dem Fall {@link BatchBookType#NONE}.
   */
  public static BatchBookType byXmlValue(String value)
  {
    if (value == null)
      return NONE;

    final BatchBookType t = byValue(Boolean.valueOf(value));
    return t != null ? t : NONE;
  }
  
  /**
   * Liefert den passenden Batchbook-Typ fuer den angegebenen Wert.
   * @param value der Wert.
   * @return die zugehoerige Enum.
   */
  public static BatchBookType byValue(Boolean value)
  {
    for (BatchBookType type:BatchBookType.values())
    {
      if (value == type.bv) // fuer den NULL Fall.
        return type;

      if (value != null && value.equals(type.bv))
        return type;
    }
    
    return null;
  }

  @Override
  public String toString()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return this.name() + ": " + i18n.tr(this.getDescription());
  }
}


