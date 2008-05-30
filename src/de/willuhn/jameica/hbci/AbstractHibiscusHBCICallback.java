/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/AbstractHibiscusHBCICallback.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/05/30 12:31:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.passport.HBCIPassport;

/**
 * Abstrakte Basis-Implementierung des HBCI-Callback.
 * Ermoeglicht gemeinsamen Code in Hibiscus und Payment-Server.
 */
public abstract class AbstractHibiscusHBCICallback extends AbstractHBCICallback
{
  /**
   * Speichert die BPD/UPD des Passports in der Hibiscus-Datenbank zwischen und aktualisiert sie automatisch bei Bedarf.
   * Dadurch stehen sie in Hibiscus zur Verfuegung, ohne dass hierzu ein Passport geoeffnet werden muss.
   * @param passport der betreffende Passport.
   */
  protected void cacheData(HBCIPassport passport)
  {
    if (passport == null)
      return;
    
    if (passport.getBPDVersion() != null)
      updateBPD(passport);
    
    if (passport.getUPDVersion() != null)
      updateUPD(passport);

  }
  
  /**
   * Aktualisiert die BPD.
   * @param bpd
   */
  private void updateBPD(HBCIPassport passport)
  {
    
  }

  /**
   * Aktualisiert die UPD.
   * @param upd
   */
  private void updateUPD(HBCIPassport passport)
  {
    
  }

}


/*********************************************************************
 * $Log: AbstractHibiscusHBCICallback.java,v $
 * Revision 1.2  2008/05/30 12:31:41  willuhn
 * @N Erster Code fuer gecachte BPD/UPD
 *
 * Revision 1.1  2008/05/30 12:01:37  willuhn
 * @N Gemeinsame Basisimplementierung des HBCICallback in Hibiscus und Payment-Server
 *
 **********************************************************************/