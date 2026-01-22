/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.experiments;

import org.hbci4java.hbci.manager.Feature;

/**
 * Implementierung fuer ein HBCI4Java-Feature.
 */
public class HBCI4JavaFeatureSkipOneStepSCA extends AbstractHBCI4JavaFeature
{
  /**
   * ct.
   */
  public HBCI4JavaFeatureSkipOneStepSCA()
  {
    super(Feature.PINTAN_INIT_SKIPONESTEPSCA);
  }
  
  @Override
  public String getDescription()
  {
    return i18n.tr("Personalisierte FinTS-Dialoginitialisierung ohne HKTAN senden, wenn noch kein TAN-Medium ausgewählt wurde");
  }

}


