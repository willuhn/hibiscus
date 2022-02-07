/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.experiments;

import org.kapott.hbci.manager.Feature;

/**
 * Implementierung fuer ein HBCI4Java-Feature.
 */
public class HBCI4JavaFeatureInitFlipUserInst extends AbstractHBCI4JavaFeature
{

  /**
   * ct.
   */
  public HBCI4JavaFeatureInitFlipUserInst()
  {
    super(Feature.INIT_FLIP_USER_INST);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.experiments.AbstractHBCI4JavaFeature#getDescription()
   */
  @Override
  public String getDescription()
  {
    return i18n.tr("Kehrt die Reihenfolge des Abrufs von BPD und UPD in der Dialoginitialisierung um.\n" + 
                   "Die Aktivierung des Features kann beim Anlegen eines Postbank-Zugangs helfen, die korrekte Liste der verfügbaren TAN-Verfahren zu ermitteln.");
  }

}


