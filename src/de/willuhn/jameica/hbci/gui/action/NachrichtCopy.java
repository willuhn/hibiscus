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

import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Kopiert den Text einer Nachricht in die Zwischenablage.
 */
public class NachrichtCopy extends CopyClipboard
{

  /**
   * Als Context kann ein Objekt vom Typ Nachricht uebergeben werden.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Nachricht))
      return;

    try
    {
      String text = ((Nachricht)context).getNachricht();
      super.handleAction(text);
    }
    catch (Exception e)
    {
      Logger.error("unable to copy text to clipboard",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Kopieren in die Zwischenablage ."),StatusBarMessage.TYPE_ERROR));
    }
  }
}
