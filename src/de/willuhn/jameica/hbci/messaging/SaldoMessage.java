/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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