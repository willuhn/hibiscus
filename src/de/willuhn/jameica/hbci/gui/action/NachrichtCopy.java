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

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Kopiert den Text einer Nachricht in die Zwischenablage.
 */
public class NachrichtCopy implements Action
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

      final Clipboard cb = new Clipboard(GUI.getDisplay());
      cb.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Text in Zwischenablage kopiert."),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("unable to copy text to clipboard",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Kopieren in die Zwischenablage ."),StatusBarMessage.TYPE_ERROR));
    }
  }

}

/**********************************************************************
 * $Log: NachrichtCopy.java,v $
 * Revision 1.1  2009/07/17 08:42:57  willuhn
 * @N Detail-Ansicht fuer Systemnachrichten der Bank
 * @N Systemnachrichten in Zwischenablage kopieren
 *
 **********************************************************************/