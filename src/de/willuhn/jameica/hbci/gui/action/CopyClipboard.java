/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Kopiert den übergebenen Text in die Zwischenablage.
 */
public class CopyClipboard implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;

    final String s = context.toString();
    
    if (s == null)
      return;
    
    try
    {
      final Clipboard cb = new Clipboard(GUI.getDisplay());
      cb.setContents(new Object[]{s}, new Transfer[]{TextTransfer.getInstance()});
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("In Zwischenablage kopiert."),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("unable to copy text to clipboard",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Kopieren in die Zwischenablage ."),StatusBarMessage.TYPE_ERROR));
    }

  }
}
