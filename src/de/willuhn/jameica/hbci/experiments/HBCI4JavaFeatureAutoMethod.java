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
public class HBCI4JavaFeatureAutoMethod extends AbstractHBCI4JavaFeature
{

  /**
   * ct.
   */
  public HBCI4JavaFeatureAutoMethod()
  {
    super(Feature.PINTAN_INIT_AUTOMETHOD);
  }
  
  @Override
  public String getDescription()
  {
    return i18n.tr("Versuchen, das TAN-Verfahren automatisch zu ermitteln, wenn die für den Benutzer verfügbaren TAN-Verfahren noch nicht übertragen wurden.\n" + 
                   "Leider funktioniert das bei einigen Banken nicht (z.B. Deutsche Bank), da diese keine personalisierte Dialog-Initialisierung mit einem Einschritt-TAN-Verfahren erlauben.");
  }

}


