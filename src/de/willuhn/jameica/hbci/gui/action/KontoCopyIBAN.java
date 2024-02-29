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

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Kopiert die IBAN des Kontos in die Zwischenablage.
 */
public class KontoCopyIBAN extends CopyClipboard
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Konto))
      return;
    
    final Konto k = (Konto) context;
    
    try
    {
      super.handleAction(StringUtils.deleteWhitespace(k.getIban()));
    }
    catch (Exception e)
    {
      Logger.error("unable to copy text to clipboard",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(Application.getI18n().tr("Fehler beim Kopieren in die Zwischenablage ."),StatusBarMessage.TYPE_ERROR));
    }

  }

}
