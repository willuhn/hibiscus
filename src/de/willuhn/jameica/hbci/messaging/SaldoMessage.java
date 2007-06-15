/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/SaldoMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/15 11:20:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;

/**
 * Wird versendet, wenn sich von einem Konto der Saldo geaendert hat.
 */
public class SaldoMessage implements Message
{
  private Konto konto = null;

  /**
   * ct.
   * @param konto das betroffene Konto.
   */
  public SaldoMessage(Konto konto)
  {
    this.konto = konto;
  }
  
  /**
   * Liefert das betroffene Konto.
   * @return das Konto.
   */
  public Konto getKonto()
  {
    return this.konto;
  }

}


/*********************************************************************
 * $Log: SaldoMessage.java,v $
 * Revision 1.1  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 **********************************************************************/