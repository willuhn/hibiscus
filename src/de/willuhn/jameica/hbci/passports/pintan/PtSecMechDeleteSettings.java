/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Setzt das TAN-Verfahren zurück.
 */
public class PtSecMechDeleteSettings implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof PinTanConfig))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen PIN/TAN-Bankzugang aus"));
    
    try
    {
      final PinTanConfig conf = (PinTanConfig) context;
      conf.setCurrentSecMech(null);
      conf.setStoredSecMech(null);
      conf.setTanMedia(null);
      conf.setChipTANUSB(null);
      conf.setConvertFlickerToQRCode(false);
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorauswahl der TAN-Verfahren zurückgesetzt"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception ex)
    {
      Logger.error("error while deleting tan settings",ex);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zurücksetzen der TAN-Verfahren"),StatusBarMessage.TYPE_ERROR));
    }
    
  }

}


