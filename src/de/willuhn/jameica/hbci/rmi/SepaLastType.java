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
 * Die verschiedenen Typen bei SEPA-Lastschriften.
 */
public enum SepaLastType
{

  /**
   * Basis-Lastschrift
   */
  CORE("Basis-Lastschrift"),
  
  /**
   * Basis-Lastschrift mit verkuerztem Vorlauf.
   */
  COR1("Basis-Lastschrift (kurzer Vorlauf)"),
  
  /**
   * B2B-Lastschrift
   */
  B2B("B2B-Lastschrift"),
  
  ;
  
  private String description = null;
  
  /**
   * ct.
   * @param description sprechender Name des Typs.
   */
  private SepaLastType(String description)
  {
    this.description = description;
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
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return this.name() + ": " + i18n.tr(this.getDescription());
  }
}


