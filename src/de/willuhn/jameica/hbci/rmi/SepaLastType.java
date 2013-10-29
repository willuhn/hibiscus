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
  CORE("LastSEPA","Basis-Lastschrift"),
  
  /**
   * Basis-Lastschrift mit verkuerztem Vorlauf.
   */
  COR1("LastCOR1SEPA","Basis-Lastschrift (kurzer Vorlauf)"),
  
  /**
   * B2B-Lastschrift
   */
  B2B("LastB2BSEPA","B2B-Lastschrift"),
  
  ;
  
  private String jobName = null;
  private String description = null;
  
  /**
   * ct.
   * @param jobName der zugehoerige HBCI-Job-Name von HBCI4Java
   * @param description sprechender Name des Typs.
   */
  private SepaLastType(String jobName, String description)
  {
    this.jobName = jobName;
    this.description = description;
  }
  
  /**
   * Liefert den zugehoerigen HBCI-Job-Namen von HBCI4Java.
   * @return der zugehoerige HBCI-Job-Name von HBCI4Java.
   */
  public String getJobName()
  {
    return this.jobName;
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


