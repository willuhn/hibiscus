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
 * Die verschiedenen Sequence-Typen bei SEPA-Lastschriften.
 */
public enum SepaLastSequenceType
{

  /**
   * Sequenz-Typ FRST fuer Erst-Einzug.
   */
  FRST("Erst-Einzug"),
  
  /**
   * Sequenz-Typ RCUR fuer Folge-Einzug.
   */
  RCUR("Folge-Einzug"),
  
  /**
   * Sequenz-Typ OOFF fuer Einmal-Einzug.
   */
  OOFF("Einmal-Einzug"),
  
  /**
   * Sequenz-Typ FNAL fuer letztmaligen Einzug.
   */
  FNAL("Letztmaliger Einzug"),
  
  ;
  
  private final String description;
  
  /**
   * ct.
   * @param description sprechender Name des Sequenz-Typs.
   */
  private SepaLastSequenceType(String description)
  {
    this.description = description;
  }
  
  /**
   * Liefert einen sprechenden Namen fuer den Sequenztyp.
   * @return sprechender Name fuer den Sequenztyp.
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


