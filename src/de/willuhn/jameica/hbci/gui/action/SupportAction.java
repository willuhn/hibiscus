/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/SupportAction.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/06/17 14:48:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
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
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    GUI.getDisplay().asyncExec(new Runnable()
    {
      /**
       * @see java.lang.Runnable#run()
       */
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