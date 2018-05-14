/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;

/**
 * Action, um einen Umsatz als ungeprueft zu markieren.
 */
public class UmsatzMarkUnChecked extends FlaggableChange
{
  /**
   * ct.
   */
  public UmsatzMarkUnChecked()
  {
    super(Umsatz.FLAG_CHECKED,false);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.action.FlaggableChange#postProcess(de.willuhn.jameica.hbci.rmi.Flaggable)
   */
  @Override
  protected void postProcess(Flaggable o) throws Exception
  {
    if (!(o instanceof Umsatz))
      return;
    
    // Wir senden die Aenderung noch per Messaging, damit SynTAX das Geprueft-Flag bei Bedarf
    // synchronisieren kann
    Application.getMessagingFactory().getMessagingQueue("hibiscus.umsatz.markchecked").sendMessage(new QueryMessage(Boolean.FALSE.toString(),o));
  }

}


