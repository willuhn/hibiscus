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
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen der Support-Seite.
 */
public class SupportAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          new Program().handleAction("http://www.willuhn.de/products/hibiscus/support.php");
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getLocalizedMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }
}
/*******************************************************************************
 * $Log: SupportAction.java,v $
 * Revision 1.1  2011/06/17 14:48:47  willuhn
 * @N Support-Link
 *
 ******************************************************************************/