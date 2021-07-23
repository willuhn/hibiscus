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
 * Die verschiedenen Typen bei SEPA-Lastschriften.
 */
public enum SepaLastType
{

  /**
   * Basis-Lastschrift
   */
  CORE("LastSEPA","MultiLastSEPA","Basis-Lastschrift"),
  
  /**
   * Basis-Lastschrift mit verkuerztem Vorlauf.
   */
  COR1("LastCOR1SEPA","MultiLastCOR1SEPA","Basis-Lastschrift (kurzer Vorlauf)"),
  
  /**
   * B2B-Lastschrift
   */
  B2B("LastB2BSEPA","MultiLastB2BSEPA","B2B-Lastschrift"),
  
  ;
  
  /**
   * Der Default-Typ (CORE).
   */
  public static SepaLastType DEFAULT = CORE;
  
  private String jobName = null;
  private String multiJobName = null;
  private String description = null;
  
  /**
   * ct.
   * @param jobName der zugehoerige HBCI-Job-Name von HBCI4Java
   * @param multiJobName der zugehoerige HBCI-Job-Name von HBCI4Java fuer den Sammel-Auftrag.
   * @param description sprechender Name des Typs.
   */
  private SepaLastType(String jobName, String multiJobName, String description)
  {
    this.jobName      = jobName;
    this.multiJobName = multiJobName;
    this.description  = description;
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
   * Liefert den zugehoerigen HBCI-Job-Namen von HBCI4Java fuer den Sammel-Auftrag.
   * @return der zugehoerige HBCI-Job-Name von HBCI4Java fuer den Sammel-Auftrag.
   */
  public String getMultiJobName()
  {
    return this.multiJobName;
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Typ.
   * @return sprechender Name fuer den Typ.
   */
  public String getDescription()
  {
    return this.description;
  }
  
  @Override
  public String toString()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    return this.name() + ": " + i18n.tr(this.getDescription());
  }
}


