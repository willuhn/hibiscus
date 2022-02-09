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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen einer System-Nachricht.
 */
public class NachrichtOpen implements Action
{

  /**
   * Als Context kann ein Objekt vom Typ Nachricht uebergeben werden.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Nachricht))
      return;

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.NachrichtDetails.class,context);
  }

}


/**********************************************************************
 * $Log: NachrichtOpen.java,v $
 * Revision 1.1  2009/07/17 08:42:57  willuhn
 * @N Detail-Ansicht fuer Systemnachrichten der Bank
 * @N Systemnachrichten in Zwischenablage kopieren
 *
 **********************************************************************/