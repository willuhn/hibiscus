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
          new Program().handleAction("http://www.willuhn.de/wiki/doku.php?id=handbuch");
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
 * Revision 1.5  2011/10/12 16:15:38  willuhn
 * @C Neue Wiki-URL
 *
 * Revision 1.4  2011-05-03 16:42:56  willuhn
 * @R Handbuch entfernt - siehe http://www.willuhn.de/blog/index.php?/archives/557-Neues-Handbuch-im-Wiki.html
 *
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
