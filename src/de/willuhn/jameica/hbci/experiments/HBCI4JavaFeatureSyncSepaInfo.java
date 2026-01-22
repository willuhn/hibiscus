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
public class HBCI4JavaFeatureSyncSepaInfo extends AbstractHBCI4JavaFeature
{

  /**
   * ct.
   */
  public HBCI4JavaFeatureSyncSepaInfo()
  {
    super(Feature.SYNC_SEPAINFO);
  }
  
  @Override
  public String getDescription()
  {
    return i18n.tr("SEPA-Informationen (u.a. die IBAN) der Konten des Bankzugangs abrufen.\n" + 
                   "Leider funktioniert das bei einigen Banken nicht (z.B. Commerzbank) - bei diesen muss die Funktion deaktiviert werden.");
  }

}


