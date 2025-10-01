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
public class HBCI4JavaFeatureVoP extends AbstractHBCI4JavaFeature
{

  /**
   * ct.
   */
  public HBCI4JavaFeatureVoP()
  {
    super(Feature.VOP);
  }
  
  @Override
  public String getDescription()
  {
    return i18n.tr("VoP (Verification of Payee) - aktiviert die ab Oktober 2025 verpflichtende Zahlungsempfängerprüfung");
  }

}
