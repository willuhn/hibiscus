/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/messaging/SaldoMessage.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/08/29 10:04:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.datasource.GenericObject;

/**
 * Wird versendet, wenn sich von einem Konto der Saldo geaendert hat.
 */
public class SaldoMessage extends ObjectMessage
{

  /**
   * ct.
   * @param object
   */
  public SaldoMessage(GenericObject object)
  {
    super(object);
  }
}


/*********************************************************************
 * $Log: SaldoMessage.java,v $
 * Revision 1.2  2007/08/29 10:04:42  willuhn
 * @N Bug 476
 *
 * Revision 1.1  2007/06/15 11:20:32  willuhn
 * @N Saldo in Kontodetails via Messaging sofort aktualisieren
 * @N Mehr Details in den Namen der Synchronize-Jobs
 * @N Layout der Umsatzdetail-Anzeige ueberarbeitet
 *
 **********************************************************************/