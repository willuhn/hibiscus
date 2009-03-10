/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/HandbuchAction.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/03/10 23:51:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.io.File;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Oeffnen des Handbuches.
 */
public class HandbuchAction implements Action
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
          AbstractPlugin p = Application.getPluginLoader().getPlugin(HBCI.class);
          new Program().handleAction(new File(p.getManifest().getPluginDir() + "/doc/hibiscus_handbuch.pdf"));
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
 * $Log: HandbuchAction.java,v $
 * Revision 1.3  2009/03/10 23:51:31  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.2  2007/08/20 15:30:28  willuhn
 * @N PostGreSqlSupport von Ralf Burger
 *
 * Revision 1.1  2007/07/23 17:46:45  jost
 * Neu: Anzeige des Handbuches
 *
 ******************************************************************************/
