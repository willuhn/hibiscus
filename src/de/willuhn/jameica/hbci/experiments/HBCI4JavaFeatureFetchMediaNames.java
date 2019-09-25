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

import org.kapott.hbci.manager.Feature;

/**
 * Implementierung fuer ein HBCI4Java-Feature.
 */
public class HBCI4JavaFeatureFetchMediaNames extends AbstractHBCI4JavaFeature
{

  /**
   * ct.
   */
  public HBCI4JavaFeatureFetchMediaNames()
  {
    super(Feature.PINTAN_INIT_FETCHMEDIANAMES);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.experiments.AbstractHBCI4JavaFeature#getDescription()
   */
  @Override
  public String getDescription()
  {
    return i18n.tr("Abruf der TAN-Medienbezeichnungen direkt bei der Einrichtung des Bankzugangs");
  }

}


